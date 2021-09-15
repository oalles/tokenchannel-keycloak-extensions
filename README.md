This is the Tokenchannel's Keycloak Customization project.

Current customizations are being developed against KC version 15.

**Current KEYCLOAK_BASE_VERSION**: 15.0.1

## Docker artifact construction

### Manually
```bash
# Project Root Folder
cd tokenchannel-keycloak-extensions

# Changes in the event listeners? So jars are generated
cd tokenchannel-authenticator;mvn clean install;cd ..;

# Build docker artifact. Choose Dockerfile.debug if want to remotely debug the code
docker build -f build/Dockerfile.debug -t tokenchannel-keycloak:15-TC . --build-arg KEYCLOAK_BASE_VERSION=15.0.2
```

## Run it locally

Check this docker-compose config file. Requires a Postgresql database.

```bash
mkdir pg
sudo chown -R 1001:1001 pg
```

```yaml
version: '2.1'
services:
  db:
    restart: always
    image: "bitnami/postgresql:11"
    ports:
      - "5432:5432"
    expose:
      - 5432
    environment:
      - POSTGRESQL_PASSWORD=${DB_PWD}
      - POSTGRESQL_USERNAME=${DB_USER}
      - POSTGRESQL_DATABASE=keycloak
    volumes:
      - ./pg:/bitnami/postgresql
    healthcheck:
      test: [ "CMD-SHELL", "pg_isready -U postgres" ]
      interval: 5s
      timeout: 5s
      retries: 5

  keycloak:
    image: "tokenchannel-keycloak:15-TC"
    depends_on:
      db:
        condition: service_healthy
    environment:
      - DB_USER=${DB_USER}
      - DB_PASSWORD=${DB_PWD}
      - KEYCLOAK_USER=${KEYCLOAK_ADMIN_USER}
      - KEYCLOAK_PASSWORD=${KEYCLOAK_ADMIN_PASSWORD}
      - DB_VENDOR=postgres
      - DB_ADDR=db
      - DB_PORT=5432
      - DB_DATABASE=keycloak
      - DB_SCHEMA=public
    ports:
      - "8081:8080"
      - "8000:8000" # Debug
```

## Remote Debugging -  IDEA

We are going to provide an additional Dockerfile, `Dockerfile.debug` that creates a docker image that informs the JRE to
enable JPDA session so the application can be debugged remotely using Java Debug Wire Protocol (JWDP).

See the [Debugging Dockerfile](build/Dockerfile.debug) content.

Now in IDEA, just create a `Remote JVM debug` running configuration:

Debugger Mode: Attach To Remote JVM

Host: `127.0.0.1`   Port: `8000`

Command Line Arguments for JVM: `-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8000` same than Dockerfile.debug

Use Module Class Path: `tokenchannel-authenticator`
