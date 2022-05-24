package com.tweetapp.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

	private static final Logger logger = LoggerFactory
			.getLogger(EmailServiceImpl.class);
	@Autowired
	private JavaMailSender javaMailSender;

	@Override
	public void sendTextEmail(String emailId,String otp) {
		logger.info("Simple Email sending start");

		SimpleMailMessage simpleMessage = new SimpleMailMessage();
		simpleMessage.setTo(emailId);
		simpleMessage.setSubject("Your Otp");
		simpleMessage.setText(otp);
		javaMailSender.send(simpleMessage);

		logger.info("Simple Email sent");

	}
}