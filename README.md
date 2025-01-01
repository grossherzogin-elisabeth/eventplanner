# Eventplanner

The eventplanner is a web application for planning multi day events with a variing event team.


## Configuration

The following settings can be changed via environment variables:

| Variable                 | Description                                                                   | Default value                 | Optional |
|--------------------------|-------------------------------------------------------------------------------|-------------------------------|----------| 
| OAUTH_ISSUER_URI         | OIDC issuer uri                                                               | -                             |          |
| OAUTH_CLIENT_ID          | OIDC client id                                                                | -                             |          |
| OAUTH_CLIENT_SECRET      | OIDC client secret                                                            | -                             |          |
| PORT                     | port this service will run on                                                 | 80                            | x        |
| PROTOCOL                 | http or https                                                                 | https                         | x        |
| HOST                     | Domain the service runs on                                                    | localhost                     | x        |
| HOST_URL                 | Full publicly reachable url, usefull when PORT is not the publicly reachable port | ${PROTOCOL}://${HOST}/${PORT} | x        |
| DATA_ENCRYPTION_PASSWORD | Password to use for PII data encryption                                       | default-encryption-password   | x        |
| EMAIL_WHITELIST          | Send email notifications only to whitelisted emails                           | -                             | x        |
| ADMIN_EMAILS             | Make all users with on of these emails to admins                              | -                             | x        |


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

```bash
docker run \
    -e ADMIN_EMAILS=your@email.de \
    -e OAUTH_ISSUER_URI=xxx \
    -e OAUTH_CLIENT_ID=xxx \
    -e OAUTH_CLIENT_SECRET=xxx \
    -e PROTOCOL=http \
    -e HOST_URL=http://localhost:8080 \
    -p 8080:80 ghcr.io/grossherzogin-elisabeth/eventplanner:latest
```

## Running from source

You can also clone this repository and run everything from source directly. For this we are using the `local` profile
in Spring, which has some slightly different default values than the ones mentioned above.

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
```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=local,secrets'
```

### Starting the frontend

Run the following commands to start the frontend on port 8090.
```bash
cd frontend
npm install
npm run dev
```

