# Eventplanner

The eventplanner is a web application for planning multi day events with a variing event team.

## Local development

### Prerequisites 

In order to run the application locally, you need Java 22 and Node 22 installed. You also need an OIDC provider for the login. This can for example be AWS cognito, Login with Google or if you prefer a self hosted alternative: a local Keycloak.

### Start the backend

The following commands will start the backend on port 8081 with a SQLite database.
```
cd backend
./gradlew bootRun --args='--spring.profiles.active=local,test'
```

### Start the frontend

Run the following commands to start the frontend on port 8080.
```
cd frontend
npm install
npm run dev
```

