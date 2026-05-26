# Energy Platform Deployment

This directory contains the replacement deployment assets for `/data/energy-basic`.

Runtime layout:

- `platform-basic-service.jar` on port `8090`
- `log-service.jar` on port `8091`
- `nginx:1.27-alpine` on port `80`
- existing `aicicd-mysql` container, with independent databases:
  - `energy_platform_basic`
  - `energy_log`

Run from the services repository after backend and frontend builds:

```bash
deploy/energy-platform/deploy.sh
```

Optional environment variables:

- `WEB_REPO`: sibling frontend repository path
- `REMOTE_HOST`: defaults to `10.140.1.177`
- `SSH_KEY`: defaults to `/Users/limenglong/.ssh/id_ed25519_menglong3_li`
- `REMOTE_DIR`: defaults to `/data/energy-basic`
