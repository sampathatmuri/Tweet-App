package com.tweetapp.configuration;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tweetapp.service.JwtUserDetailsService;
import com.tweetapp.util.JwtTokenUtil;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(JwtRequestFilter.class);

	@Autowired
	private JwtUserDetailsService jwtUserDetailsService;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	private static final String ERROR = "error";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException, UsernameNotFoundException {

		final String requestTokenHeader = request.getHeader("Authorization");

		String username = null;
		String jwtToken = null;
		// JWT Token is in the form "Bearer token". Remove Bearer word and get
		// only the Token
		log.info("Checking for Authorization header in request");
		if (requestTokenHeader != null) {
			log.warn("Authorization header found");
			log.info("Validating  the JWT token");
			if (requestTokenHeader.startsWith("Bearer ")) {
				log.info("JWT Token format is Valid");
				jwtToken = requestTokenHeader.substring(7);
				try {
					username = jwtTokenUtil.getUsernameFromToken(jwtToken);
				} catch (IllegalArgumentException e) {
					log.warn("Unable to get JWT Token");
					request.setAttribute(ERROR, "Session expired");
				} catch (ExpiredJwtException e) {
					log.warn("JWT Token has expired");
					request.setAttribute(ERROR, "Session expired");
				} catch (MalformedJwtException | SignatureException e) {
					log.warn("Malformed JWT Token");
					request.setAttribute(ERROR, "Session expired");
				}
			} else {
				log.warn("JWT Token does not begin with Bearer String");
				request.setAttribute(ERROR, "Provide JWT Token");
			}
		} else {
			log.warn("No authorization header found");
			request.setAttribute(ERROR, "Provide authorization header");
		}

		// Once we get the token validate it.
		if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UserDetails userDetails = this.jwtUserDetailsService.loadUserByUsername(username);

			// if token is valid configure Spring Security to manually set
			// authentication
			Boolean tokenValidated = jwtTokenUtil.validateToken(jwtToken, userDetails);
			if (Boolean.TRUE.equals(tokenValidated)) {
				log.info("JWT token is valid");
				UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());
				usernamePasswordAuthenticationToken
						.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				// After setting the Authentication in the context, we specify
				// that the current user is authenticated. So it passes the
				// Spring Security Configurations successfully.
				SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
				log.info("Authentication Successful!!");
			}
		}
		chain.doFilter(request, response);
	}

}