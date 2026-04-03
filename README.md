![GitHub Release](https://img.shields.io/github/v/release/grossherzogin-elisabeth/eventplanner?label=latest%20release)
![GitHub commit activity](https://img.shields.io/github/commit-activity/m/grossherzogin-elisabeth/eventplanner)
![checks](https://github.com/grossherzogin-elisabeth/eventplanner/actions/workflows/validate_code.yml/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=grossherzogin-elisabeth_eventplanner&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=grossherzogin-elisabeth_eventplanner)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=grossherzogin-elisabeth_eventplanner&metric=coverage)](https://sonarcloud.io/summary/new_code?id=grossherzogin-elisabeth_eventplanner)

# Eventplanner

Eventplanner is a web application for planning multi-day events with scheduling, crew management, notifications, and exports.

## Tech stack

- Backend: Java 25, Spring Boot, SQLite, Flyway
- Frontend: Vue 3 + TypeScript + Vite
- Packaging: single Docker image serving backend and built frontend

## Repository layout

- `backend/` Spring Boot application
- `frontend/` Vue application
- `data/` local database and runtime data (for local/dev usage)

## Quick start (Docker)

The published image exposes port `80` inside the container.

```bash
docker run \
  -e OAUTH_ISSUER_URI="https://your-issuer" \
  -e OAUTH_CLIENT_ID="your-client-id" \
  -e OAUTH_CLIENT_SECRET="your-client-secret" \
  -e DATA_ENCRYPTION_PASSWORD="change-me" \
  -e PROTOCOL="http" \
  -e HOST_URL="http://localhost:8080" \
  -e ADMIN_EMAILS="admin@example.com" \
  -p 8080:80 \
  ghcr.io/grossherzogin-elisabeth/eventplanner:latest
```

Open `http://localhost:8080` afterwards.

## Sample docker-compose

Use this as a starting point and move sensitive values to a local `.env` file before sharing.

```yaml
services:
  eventplanner:
    image: ghcr.io/grossherzogin-elisabeth/eventplanner:latest
    container_name: eventplanner
    ports:
      - "8080:80"
    environment:
      OAUTH_ISSUER_URI: "https://your-issuer"
      OAUTH_CLIENT_ID: "your-client-id"
      OAUTH_CLIENT_SECRET: "your-client-secret"
      DATA_ENCRYPTION_PASSWORD: "change-me"
      PROTOCOL: "http"
      HOST: "localhost"
      HOST_URL: "http://localhost:8080"
      EMAIL_WHITELIST: ""
      ADMIN_EMAILS: "admin@example.com"
    volumes:
      - ./data:/app/data
    restart: unless-stopped
```

## Run from source

### Prerequisites

- Java 25 (Temurin recommended)
- Node.js 24
- An OIDC provider (for login)

### 1) Start backend

Set OIDC values either as environment variables or in `backend/bin/main/application-secrets.yml`.

Required values:

```yaml
OAUTH_ISSUER_URI: <oidc-issuer-uri>
OAUTH_CLIENT_ID: <oidc-client-id>
OAUTH_CLIENT_SECRET: <oidc-client-secret>
```

Run backend:

```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=local,secrets'
```

### 2) Start frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on `http://localhost:8090` and proxies API/auth traffic to backend on `http://localhost:8091`.

## Build, lint, and test

### Frontend

```bash
cd frontend
npm run check:format
npm run check:lint
npm run check:typescript
npm run build
npm run test:coverage
```

### Backend

```bash
cd backend
./gradlew build test jacocoTestReport
```

## Environment variables

| Variable | Description | Default | Required |
|---|---|---|---|
| `OAUTH_ISSUER_URI` | OIDC issuer URI | - | yes |
| `OAUTH_CLIENT_ID` | OIDC client ID | - | yes |
| `OAUTH_CLIENT_SECRET` | OIDC client secret | - | yes |
| `PORT` | HTTP port used by backend service | `80` | no |
| `PROTOCOL` | Public protocol (`http` or `https`) | `https` | no |
| `HOST` | Public host name | `localhost` | no |
| `HOST_URL` | Full public URL (important when exposed port differs from app port) | `${PROTOCOL}://${HOST}/${PORT}` | no |
| `DATA_ENCRYPTION_PASSWORD` | Password used to encrypt PII data | `default-encryption-password` | strongly recommended |
| `EMAIL_WHITELIST` | Restrict outgoing email notifications to listed addresses | empty | no |
| `ADMIN_EMAILS` | Comma-separated emails automatically granted admin role | empty | no |

## Authentication notes

Eventplanner relies on external OIDC providers and does not manage local user passwords.
For local testing, Keycloak is a common self-hosted option.

## Additional docs

- `frontend/README.md`
- `backend/src/main/resources/application.yml`
