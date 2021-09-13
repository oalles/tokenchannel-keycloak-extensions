package io.tokenchannel.keycloak.authenticator;


import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.Arrays;
import java.util.List;


public class TokenChannelAuthenticatorFactory implements AuthenticatorFactory {

    @Override
    public String getId() {
        return "TC-Auth";
    }
    @Override
    public String getDisplayType() {
            return "TokenChannel - Second Step Authenticator";
    }

    @Override
    public String getReferenceCategory() {
        return "otp";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[]{
                AuthenticationExecutionModel.Requirement.REQUIRED,
                AuthenticationExecutionModel.Requirement.ALTERNATIVE,
                AuthenticationExecutionModel.Requirement.DISABLED
        };
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Validates an OTP sent to users mobile phone.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Arrays.asList(
                new ProviderConfigProperty("apiKey", "TokenChannel Api Key", "Get your apikey from TC dashboard", ProviderConfigProperty.STRING_TYPE, "TCApi  Key"),
                new ProviderConfigProperty("maxAttempts", "Max Validation Attempts", "The sender ID is displayed as the message sender on the receiving device.", ProviderConfigProperty.STRING_TYPE, 5),
                new ProviderConfigProperty("expirationInMinutes", "Expiration Time", "The time to live in seconds for the code to be valid.", ProviderConfigProperty.STRING_TYPE, "5"),
                new ProviderConfigProperty("codeLength", "Validation Code Length", "The length of the validation code to be sent", ProviderConfigProperty.STRING_TYPE, "6"),
                new ProviderConfigProperty("channelType", "Channel to use", "Valid values are: VOICE, WHATSAPP, SMS", ProviderConfigProperty.STRING_TYPE, "VOICE"),
                new ProviderConfigProperty("testMode", "Simulation mode", "In simulation mode, the SMS won't be sent, but printed to the server logs", ProviderConfigProperty.BOOLEAN_TYPE, true)
        );
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new TokenChannelAuthenticator();
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }
}
