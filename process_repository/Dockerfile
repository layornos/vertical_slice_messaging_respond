FROM openjdk:8-jdk
COPY ./build/libs/process_repository-all.jar /usr/share/java/respond/process_repository.jar
ENV BROKER="tcp://localhost"
ENV DATABASE="jdbc:mysql://localhost/respond?useUnicode=yes&amp;characterEncoding=UTF-8&amp;useJDBCCompliantTimezoneShift=true&amp;useLegacyDatetimeCode=false&amp;serverTimezone=UTC"
ENV DATABASE_USER="root"
ENV DATABASE_PASSWORD=""
CMD ["java", "-jar", "/usr/share/java/respond/process_repository.jar"]