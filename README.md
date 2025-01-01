# Eventplanner

The eventplanner is a web application for planning multi day events with a variing event team.

## Environment variables

```
PORT
HOST
DATA_ENCRYPTION_PASSWORD
OAUTH_ISSUER_URI
OAUTH_CLIENT_ID
OAUTH_CLIENT_SECRET
EMAIL_WHITELIST
ADMIN_EMAILS
```

## Local development

### Prerequisites 

In order to run the application locally, you need Java 22 and Node 22 installed. You also need an OIDC provider for the login. This can for example be AWS cognito, Login with Google or if you prefer a self hosted alternative: a local Keycloak.

### Start the backend

Create an `application-secrets.yml` with the following content:

```yaml
DATA_ENCRYPTION_PASSWORD: <some-random-password>
OAUTH_ISSUER_URI: <oidc-issuer-uri>
OAUTH_CLIENT_ID: <oidc-client-id>
OAUTH_CLIENT_SECRET: <oidc-client-secret>
EMAIL_WHITELIST: <your-email-address>
ADMIN_EMAILS: <your-email-address>
```

Then use the following commands to start the backend on port 8091 with a SQLite database.
```
cd backend
./gradlew bootRun --args='--spring.profiles.active=local'
```

### Start the frontend

Run the following commands to start the frontend on port 8090.
```
cd frontend
npm install
npm run dev
```

