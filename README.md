# energy-platform-services

Backend services and deployment assets for the energy platform.

## Scope

- `platform-basic-service`: platform management and basic information service.
- `log-service`: operation and login log service.
- `deploy/energy-platform`: deployment scripts, Nginx configuration, and smoke tests.

## Architecture Constraints

- Each backend service owns an independent MySQL 8 database.
- Backend services exchange data through APIs only.
- Platform management and basic information are delivered in one backend service for the first release.
- Operation and login logs are written through the log service API.

## Runtime Targets

- Java 17+
- Spring Boot 3.x
- MySQL 8
- Docker runtime on the deployment host
