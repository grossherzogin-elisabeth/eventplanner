# build frontend
FROM node:23.11 AS frontend-builder
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

ARG COMMIT="?"
ARG BRANCH="main"
ARG TIME="1970-01-01 00:00:00"
ENV BUILD_COMMIT=${COMMIT}
ENV BUILD_BRANCH=${BRANCH}
ENV BUILD_TIME=${TIME}

WORKDIR /app
RUN mkdir -p /app/data
COPY --from=backend-builder /builder/build/libs/*.jar eventplanner.jar
ENTRYPOINT ["java","-XX:+UseContainerSupport","-jar","/app/eventplanner.jar"]
