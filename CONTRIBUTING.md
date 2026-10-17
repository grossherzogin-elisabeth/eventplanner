## Running the application from source

### Start the backend

Create a `backend/bin/main/application-local.yml` and fill the required values. The minimal configuration needs the
following variables set:

```yaml
OAUTH_ISSUER_URI: <oidc-issuer-uri>
OAUTH_CLIENT_ID: <oidc-client-id>
OAUTH_CLIENT_SECRET: <oidc-client-secret>
EMAIL_WHITELIST: your@email.com
ADMIN_EMAILS: your@email.com
```

Run the backend:

```bash
cd backend
MODE=local ./gradlew bootRun'
```

### Start the frontend

```bash
cd frontend
npm install
npm run dev
```

By default, the backend runs on `http://localhost:8081` and the frontend on `http://localhost:8080`. You can change the
ports or hostname in the `application-local.yml` and the `.env.local` files. API and auth traffic is proxied to the
frontend via vite config.

## Build, lint, and test

If you want to contribute, make sure your changes pass the following checks:

### Frontend

```bash
npm run check:format
npm run check:lint
npm run check:typescript
npm run build
npm run test:coverage
```

### Backend

```bash
./gradlew build test jacocoTestReport
```
