package io.tokenchannel.keycloak.authenticator;

import io.tokenchannel.ChallengeOptions;
import io.tokenchannel.ChallengeResponse;
import io.tokenchannel.ChannelType;
import io.tokenchannel.TokenChannel;
import io.tokenchannel.exceptions.ChallengeExpiredException;
import io.tokenchannel.exceptions.InvalidCodeException;
import io.tokenchannel.exceptions.MaxAttemptsExceededException;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

@Slf4j
public class TokenChannelAuthenticator implements Authenticator {

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        AuthenticationFlowContextHandler contextHandler = new AuthenticationFlowContextHandler(context);

        TokenChannel tokenChannel = contextHandler.getTokenChannelClient();
        ChannelType channelType = contextHandler.getChannelType(); // SMS, VOICE, WHATSAPP
        String phonenumber = contextHandler.getUserPhonenumber();
        ChallengeOptions challengeOptions = contextHandler.getChallengeOptions();

        try {
            ChallengeResponse response = tokenChannel.challenge(channelType, phonenumber, challengeOptions);
            contextHandler.storeRequestId(response.getRequestId());
            contextHandler.goToValidationForm();
        } catch (Exception e) {
            log.error("Execution not attempted. ", e);
            contextHandler.displayInternalError("validationCodeNotSent", e.getClass().getSimpleName());
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) {

        AuthenticationFlowContextHandler contextHandler = new AuthenticationFlowContextHandler(context);

        TokenChannel tokenChannel = contextHandler.getTokenChannelClient();
        String requestId = contextHandler.getRequestId();
        String validationCode = contextHandler.getValidationCode();
        try {
            tokenChannel.authenticate(requestId, validationCode);
            contextHandler.successfulExecution();
        } catch (Throwable e) {
            if (e.getClass().isAssignableFrom(InvalidCodeException.class)) {
                // Same form + error label
                contextHandler.displayInvalidCode();
                return; // Challenge not finalized
            } else if (e.getClass().isAssignableFrom(ChallengeExpiredException.class)) {
                contextHandler.displayChallengeExpired();
            } else if (e.getClass().isAssignableFrom(MaxAttemptsExceededException.class)) {
                contextHandler.displayMaxAttemptsExceeded();
            } else {
                contextHandler.displayInternalError("verificationErr", e.getClass().getSimpleName());
            }

            // Challenge finalized - The authenticator was attempted, but not fulfilled
            AuthenticationExecutionModel execution = context.getExecution();
            if (execution.isConditional() || execution.isAlternative()) {
                context.attempted();
            }
        }
    }


    @Override
    public boolean requiresUser() {
        // 2 Step auth - So first step must provide user
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return user.getFirstAttribute(AuthenticationFlowContextHandler.PHONENUMBER_ATTRIBUTE) != null;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    @Override
    public void close() {
    }
}
