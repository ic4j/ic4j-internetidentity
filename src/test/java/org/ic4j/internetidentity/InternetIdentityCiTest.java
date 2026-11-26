package org.ic4j.internetidentity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.ic4j.agent.Agent;
import org.ic4j.agent.AgentBuilder;
import org.ic4j.agent.NonceFactory;
import org.ic4j.agent.http.ReplicaApacheHttpTransport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "IC4J_CI_FIXTURE", matches = "^[a-z0-9]+(-[a-z0-9]+)+$")
class InternetIdentityCiTest {
    @Test
    @Timeout(90)
    void deployedInternetIdentityRespondsToStatsAndChallenge() throws Exception {
        Properties fixture = new Properties();
        fixture.setProperty("ii.canister", System.getenv("IC4J_CI_FIXTURE"));
        // Fixed loopback endpoint; cannot target a public replica.
        Agent agent = new AgentBuilder()
                .transport(ReplicaApacheHttpTransport.create("http://127.0.0.1:4943/"))
                .nonceFactory(new NonceFactory()).build();
        try {
            agent.fetchRootKey();
            InternetIdentityService service = InternetIdentityService.create(agent, fixture);
            assertEquals(0L, service.stats().usersRegistered.longValue());
            Challenge challenge = service.createChallenge().get(60, TimeUnit.SECONDS);
            assertNotNull(challenge);
            assertNotNull(challenge.challengeKey);
            assertFalse(challenge.challengeKey.isEmpty());
            assertNotNull(InternetIdentityService.getCaptchaImage(challenge));
        } finally {
            agent.close();
        }
    }
}
