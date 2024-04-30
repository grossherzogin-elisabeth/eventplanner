# build frontend
FROM node:20.12.2 as frontend-builder
WORKDIR /builder
COPY ./frontend .
RUN npm install && npm run build

# build backend
FROM eclipse-temurin:21-jdk-alpine AS backend-builder
WORKDIR /builder
COPY ./backend .
COPY --from=frontend-builder /builder/dist/ ./src/main/resources/static/
RUN ./gradlew build

# combine frontend and backend
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=backend-builder /builder/build/libs/*.jar eventplanner.jar
ENTRYPOINT ["java","-XX:+UseContainerSupport","-jar","/app/eventplanner.jar","--spring.profiles.active=prod"]
