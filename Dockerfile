FROM openjdk:17-jdk-alpine
RUN addgroup -S spring && adduser -S spring -G spring
RUN mkdir -p /app/uploads && chown -R spring:spring /app/uploads
RUN mkdir -p /app/pdf && chown -R spring:spring /app/pdf
RUN mkdir -p /app/ocr && chown -R spring:spring /app/ocr
WORKDIR /app
USER spring:spring
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","app.jar"]