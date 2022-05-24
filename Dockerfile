FROM openjdk:11
EXPOSE 8082
ADD target/fse-tweet-app.jar fse-tweet-app.jar 
ENTRYPOINT ["java","-jar","/fse-tweet-app.jar"]