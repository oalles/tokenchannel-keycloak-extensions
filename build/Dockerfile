ARG KEYCLOAK_BASE_VERSION
FROM jboss/keycloak:${KEYCLOAK_BASE_VERSION}
COPY tokenchannel-authenticator/target/tokenchannel-authenticator.jar /opt/jboss/keycloak/standalone/deployments/
