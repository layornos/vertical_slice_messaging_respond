FROM openjdk:8-jdk
COPY ./selfhealing-all.jar /usr/share/java/respond/selfhealing.jar
ENV RESPOND_SELFHEALING_BROKER="tcp://localhost"
CMD ["java", "-jar", "/usr/share/java/respond/selfhealing.jar"]
