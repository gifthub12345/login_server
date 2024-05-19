FROM amazoncorretto:17.0.8-alpine as build
ENV APP_HOME=/app
WORKDIR $APP_HOME
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
RUN chmod +x gradlew
RUN apk add dos2unix
RUN dos2unix ./gradlew
RUN ./gradlew build || return 0
COPY src ./src
RUN ./gradlew clean bootJar

FROM amazoncorretto:17.0.8-alpine
ENV APP_HOME=/app
ENV GOOGLE_CLIENT_ID=default
ENV GOOGLE_CLIENT_SECRET=default
ENV GOOGLE_REDIRECT_URL=default
ENV APPLE_REDIRECT_URL=default
ENV APPLE_CLIENT_ID=default
ENV APPLE_TEAM_ID=default
ENV APPLE_KEY_ID=default
ENV DATABASE_ENDPOINT=default
ENV DATABASE_USERNAME=default
ENV DATABASE_PASSWORD=default
ENV DB_NAME=default
ENV REDIS_HOST=default
WORKDIR $APP_HOME
ARG JAR_FILE=build/libs/*.jar
COPY --from=build $APP_HOME/$JAR_FILE ./app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]