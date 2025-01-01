# Eventplanner

The eventplanner is a web application for planning multi day events with a variing event team.


## Configuration

The following settings can be changed via environment variables:

```
PORT
HOST
HOST_URL
DATA_ENCRYPTION_PASSWORD
OAUTH_ISSUER_URI
OAUTH_CLIENT_ID
OAUTH_CLIENT_SECRET
EMAIL_WHITELIST
ADMIN_EMAILS
```


## Auth

This app does not provide a custom auth management. Instead, we rely on external OIDC providers for the login. 
For just testing things out this can be an [AWS cognito](https://aws.amazon.com/de/cognito/), which has a very
generous free tier of 10k users. If you prefer a self-hosted alternative you can use a local
[Keycloak](https://www.keycloak.org/). In every case you need the Authorization Code Flow enabled and the 
`client id` and `client secret` set as environment variables. 


## Running via Docker

The docker image runs the app on port 80 by default. You can run the service on another port using the `PORT`
environment variable. If public and internal port are not the same, make sure to also pass the `HOST_URL` variable,
so that the backend knows how it is accessible in the browser, e.g. for OAuth redirects. 

```
docker run \
    -e ADMIN_EMAILS=your@email.de \
    -e OAUTH_ISSUER_URI=xxx \
    -e OAUTH_CLIENT_ID=xxx \
    -e OAUTH_CLIENT_SECRET=xxx \
    -e PORT=http://localhost:8080 \
    -p 8080:80 ghcr.io/grossherzogin-elisabeth/eventplanner:latest
```

## Running from source

### Prerequisites 

In order to run the application locally, you need Java 21 and Node 22 installed. You also need an OIDC provider
for the login.

### Starting the backend

Before starting the backend locally, you have to set a few environment variables. The easiest way to do this is 
to create an `application-secrets.yml`, but you can also set the variables individually. The following environment
variables are required:

```yaml
OAUTH_ISSUER_URI: <oidc-issuer-uri>
OAUTH_CLIENT_ID: <oidc-client-id>
OAUTH_CLIENT_SECRET: <oidc-client-secret>
EMAIL_WHITELIST: <your-email-address>
ADMIN_EMAILS: <your-email-address>
```

Then use the following commands to start the backend on port 8091 with a SQLite database.
```
cd backend
./gradlew bootRun --args='--spring.profiles.active=local,secrets'
```

### Starting the frontend

Run the following commands to start the frontend on port 8090.
```
cd frontend
npm install
npm run dev
```

