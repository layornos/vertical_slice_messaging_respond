FROM openjdk:8-jdk
COPY ./build/libs/process_repository-all.jar /usr/share/java/respond/process_repository.jar
ENV RESPOND_REPOSITORY_BROKER="tcp://localhost"
ENV RESPOND_REPOSITORY_DATABASE="jdbc:mysql://localhost/respond?useUnicode=yes&characterEncoding=UTF-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC"
ENV RESPOND_REPOSITORY_DATABASE_USER="root"
ENV RESPOND_REPOSITORY_DATABASE_PASSWORD=""
CMD ["java", "-jar", "/usr/share/java/respond/process_repository.jar"]