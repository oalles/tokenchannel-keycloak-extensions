package io.tokenchannel.keycloak.authenticator;

import io.tokenchannel.*;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;

import javax.ws.rs.core.Response;
import java.util.Locale;

/**
 * Higher-level interface to the context that makes it easier to use
 */
public class AuthenticationFlowContextHandler {

    public static final String TC_AUTH_TEMPLATE = "tc-auth.ftl";
    public static final String PHONENUMBER_ATTRIBUTE = "phonenumber";
    public static final String REQUEST_ID = "requestId";
    public static final String VALIDATION_CODE_PARAM = "validation_code";

    private final AuthenticationFlowContext context;
    private TokenChannel tokenChannel;

    public AuthenticationFlowContextHandler(final AuthenticationFlowContext context) {
        this.context = context;
    }

    public String getUserPhonenumber() {
        UserModel user = context.getUser();
        return user.getFirstAttribute(PHONENUMBER_ATTRIBUTE);
    }

    public TokenChannel getTokenChannelClient() {
        if (tokenChannel == null) {

            AuthenticatorConfigModel config = context.getAuthenticatorConfig();
            String apiKey = config.getConfig().get("apiKey");
            Boolean testModeEnabled = Boolean.parseBoolean(config.getConfig().get("testMode"));

            TokenChannelProperties properties = new TokenChannelProperties();
            properties.setApiKey(apiKey);
            properties.setTestMode(testModeEnabled);
            properties.setTimeoutInSeconds(10);

            tokenChannel = new TokenChannel(properties);
        }
        return tokenChannel;
    }

    private String resolveLanguage() {
        UserModel user = context.getUser();
        KeycloakSession session = context.getSession();
        Locale userLocale = session.getContext().resolveLocale(user);
        String language = "en";
        if (userLocale != null && getTokenChannelClient().getSupportedLanguages().stream().anyMatch(l -> l.equals(userLocale.getLanguage()))) {
            language = userLocale.getLanguage();
        }
        return language;
    }

    public ChallengeOptions getChallengeOptions() {

        String language = resolveLanguage();

        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        Integer expirationInMinutes = Integer.parseInt(config.getConfig().get("expirationInMinutes"));
        Integer maxAttempts = Integer.parseInt(config.getConfig().get("maxAttempts"));
        Integer codeLength = Integer.parseInt(config.getConfig().get("codeLength"));

        return ChallengeOptions.builder()
                .language(language)
                .expirationInMinutes(expirationInMinutes)
                .maxAttempts(maxAttempts)
                .codeLength(codeLength)
                .charset(CodeCharSet.UPPER)
                .build();
    }

    public ChannelType getChannelType() {
        AuthenticatorConfigModel config = context.getAuthenticatorConfig();
        String channelType = config.getConfig().get("channelType");
        try {
            return ChannelType.valueOf(channelType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ChannelType.SMS;
        }
    }

    public void goToValidationForm() {
        context.challenge(context.form().setAttribute("realm", context.getRealm()).createForm(TC_AUTH_TEMPLATE));
    }

    public void successfulExecution() {
        context.success();
    }

    public String getValidationCode() {
        return context.getHttpRequest().getDecodedFormParameters().getFirst(VALIDATION_CODE_PARAM);
    }

    public void storeRequestId(String requestId) {
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        authSession.setAuthNote(REQUEST_ID, requestId);
    }

    public String getRequestId() {
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        return authSession.getAuthNote(REQUEST_ID);
    }

    public void displayInvalidCode() {
        context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS,
                context.form().setAttribute("realm", context.getRealm())
                        .setError("invalidCode").createForm(AuthenticationFlowContextHandler.TC_AUTH_TEMPLATE));
    }

    public void displayChallengeExpired() {
        context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE,
                context.form().setError("challengeExpired").createErrorPage(Response.Status.UNAUTHORIZED));
    }

    public void displayMaxAttemptsExceeded() {
        context.failureChallenge(AuthenticationFlowError.ACCESS_DENIED,
                context.form().setError("challengeMaxAttemptsExceed").createErrorPage(Response.Status.UNAUTHORIZED));
    }

    public void displayInternalError(String key, String description) {
        context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
                context.form().setError(key, description)
                        .createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
    }
}
