![GitHub Release](https://img.shields.io/github/v/release/grossherzogin-elisabeth/eventplanner?label=latest%20release)
![GitHub commit activity](https://img.shields.io/github/commit-activity/m/grossherzogin-elisabeth/eventplanner)
![checks](https://github.com/grossherzogin-elisabeth/eventplanner/actions/workflows/validate_code.yml/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=grossherzogin-elisabeth_eventplanner&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=grossherzogin-elisabeth_eventplanner)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=grossherzogin-elisabeth_eventplanner&metric=coverage)](https://sonarcloud.io/summary/new_code?id=grossherzogin-elisabeth_eventplanner)

# Open Source Crew and Sailing Leg Planning

Manage your crew and plan sailing legs with minimum effort. Crew planner offers all you need to plan your crews, manage
certificates and organize your sailing year.

## Getting started

To run the crew planner, we recommend to use our Docker image for quick setup. Use the following command for a quick
start:

```bash
docker run \
  -e OAUTH_ISSUER_URI="https://your-issuer" \
  -e OAUTH_CLIENT_ID="your-client-id" \
  -e OAUTH_CLIENT_SECRET="your-client-secret" \
  -e EMAIL_WHITELIST="admin@example.com" \
  -e ADMIN_EMAILS="admin@example.com" \
  -p 8080:80 \
  ghcr.io/grossherzogin-elisabeth/eventplanner:latest
```

Or if you prefer to use `docker-compose`, create a `docker-compose.yml` file with the following content:

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
      HOST_URL: "http://localhost:8080"
      EMAIL_WHITELIST: "admin@example.com"
      ADMIN_EMAILS: "admin@example.com"
    volumes:
      - ./data:/app/data
    restart: unless-stopped
```

### Configuration

The application can be configured using environment variables.

### Required Environment Variables

The following environment variables are required to run the application:

- **OAUTH_ISSUER_URI**
- **OAUTH_CLIENT_ID**
- **OAUTH_CLIENT_SECRET**

The following environment variables are not required, but highly recommended to set for production use:

- **ACCESS_KEY_HASH_SECRET**
- **DATA_ENCRYPTION_PASSWORD**
- **HOST_URL**

### All Environment Variables

- **ACCESS_KEY_HASH_SECRET**: The secret used for hashing access keys.
- **ACCESS_KEY_EXPIRATION**: How long access keys are valid. Defaults to `P21D` (21 days).
- **ADMIN_EMAILS**: Comma-separated emails automatically granted admin role. Recommended to get admin access with your
  first user.
- **CSRF_ENABLED**: Whether CSRF protection is enabled. Defaults to `true`.
- **DATA_DIRECTORY**: The directory where data is stored. Defaults to `/app/data`. When using Docker, you typically
  don't change this, but mount a volume to this path to persist data.
- **DATA_ENCRYPTION_PASSWORD**: The secret used to encrypt sensitive data like user information. It is recommended to
  change this for production use.
- **EMAIL_WHITELIST**: Comma-separated list of email addresses. When set, notifications are only sent to these email
  addresses.
- **EMAIL_PREFIX**: A prefix for all outgoing email notifications. Mainly used to easily distinguish between different
  environments (e.g. `[TEST]`). By default, no prefix is used.
- **HOST_URL**: The full publicly reachable URL of the application. E.g. https://events.your-domain.com
- **LOG_LEVEL:** Default log level for all application logs. Supported values are `DEBUG`, `INFO`, `WARN` and `ERROR`.
  Defaults to `INFO`.
- **OAUTH_ISSUER_URI:** OIDC issuer URI.
- **OAUTH_CLIENT_ID:** OIDC client ID.
- **OAUTH_CLIENT_SECRET:** OIDC client secret.
- **PORT**: The port on which the application listens. Defaults to `80`.
