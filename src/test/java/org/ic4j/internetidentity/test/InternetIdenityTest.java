package org.ic4j.internetidentity.test;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.Security;
import java.util.Base64;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.ic4j.agent.Agent;
import org.ic4j.agent.AgentBuilder;
import org.ic4j.agent.ReplicaTransport;
import org.ic4j.agent.http.ReplicaApacheHttpTransport;
import org.ic4j.agent.identity.BasicIdentity;
import org.ic4j.agent.identity.Identity;
import org.ic4j.internetidentity.AddTentativeDeviceResponse;
import org.ic4j.internetidentity.Challenge;
import org.ic4j.internetidentity.ChallengeResult;
import org.ic4j.internetidentity.DeviceData;
import org.ic4j.internetidentity.GetDelegationResponse;
import org.ic4j.internetidentity.IdentityAnchorInfo;
import org.ic4j.internetidentity.InternetIdentityService;
import org.ic4j.internetidentity.InternetIdentityStats;
import org.ic4j.internetidentity.KeyType;
import org.ic4j.internetidentity.PrepareDelegationResponse;
import org.ic4j.internetidentity.Purpose;
import org.ic4j.internetidentity.RegisterResponse;
import org.ic4j.internetidentity.VerifyTentativeDeviceResponse;
import org.ic4j.types.Principal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class InternetIdenityTest {
	static Logger LOG;

	static String PROPERTIES_FILE_NAME = "application.properties";

	static {
		LOG = LoggerFactory.getLogger(InternetIdenityTest.class);
	}

	@Test
	public void test() {

		try {
			Security.addProvider(new BouncyCastleProvider());

			InputStream propInputStream = InternetIdenityTest.class.getClassLoader()
					.getResourceAsStream(PROPERTIES_FILE_NAME);

			Properties env = new Properties();
			env.load(propInputStream);

			String iiLocation = env.getProperty("ii.location");

			ReplicaTransport transport = ReplicaApacheHttpTransport.create(iiLocation);

			KeyPair keyPair = InternetIdentityService.generateSessionKey();
			Identity identity = BasicIdentity.fromKeyPair(keyPair);

			// Identity identity = BasicIdentity.fromPEMFile(Paths.get("identity.pem"));

			Agent agent = new AgentBuilder().transport(transport).identity(identity).build();

			InternetIdentityService internetIdentityService = InternetIdentityService.create(agent, env);

			byte[] sessionKey = keyPair.getPublic().getEncoded();

			CompletableFuture<Challenge> challengeResponse = internetIdentityService.createChallenge();

			Challenge challenge = challengeResponse.get();

			LOG.info(challenge.pngBase64);
			LOG.info(challenge.challengeKey);

			Path captchaFile = Paths.get("challenge.png");
			// convert byte[] back to a BufferedImage
			BufferedImage captchaImage = internetIdentityService.getCaptchaImage(challenge);


			ChallengeResult challengeResult = new ChallengeResult();

			challengeResult.challengeKey = challenge.challengeKey;
			challengeResult.chars = "a";

			DeviceData device = new DeviceData();

			device.alias = "Device 1";
			device.pubkey = sessionKey;

			Purpose purpose = Purpose.authentication;

			device.purpose = purpose;

			KeyType keyType = KeyType.platform;
			device.keyType = keyType;

			CompletableFuture<RegisterResponse> registerResponse = internetIdentityService.register(device,
					challengeResult);

			RegisterResponse register = registerResponse.get();
			LOG.info(register.name());
			LOG.info(register.registeredValue.userNumber.toString());

			Long userNumber = register.registeredValue.userNumber;

			String frontendHostname = "http://0.0.0.0:8000/?canisterId=rdmx6-jaaaa-aaaaa-aaadq-cai&id=renrk-eyaaa-aaaaa-aaada-cai";

			CompletableFuture<PrepareDelegationResponse> response = internetIdentityService
					.prepareDelegation(userNumber, frontendHostname, sessionKey, Optional.empty());

			PrepareDelegationResponse prepareDelegationResponse = response.get();

			LOG.info(Base64.getEncoder().encodeToString(prepareDelegationResponse.userKey));

			GetDelegationResponse getDelegationResponse = internetIdentityService.getDelegation(userNumber,
					frontendHostname, sessionKey, prepareDelegationResponse.timestamp);

			LOG.info(Base64.getEncoder().encodeToString(getDelegationResponse.signedDelegation.signature));

			Principal principal = internetIdentityService.getPrincipal(userNumber, frontendHostname);

			LOG.info(principal.toString());

			keyPair = InternetIdentityService.generateSessionKey();

			device.alias = "Device 2";
			device.pubkey = keyPair.getPublic().getEncoded();

			Long enterDeviceRegistrationModeResponse = internetIdentityService.enterDeviceRegistrationMode(userNumber)
					.get();

			AddTentativeDeviceResponse addTentativeDeviceResponse = internetIdentityService
					.addTentativeDevice(userNumber, device).get();

			LOG.info(addTentativeDeviceResponse.name());

			LOG.info(addTentativeDeviceResponse.addedTentatively.verificationCode);

			VerifyTentativeDeviceResponse verifyTentativeDeviceResponse = internetIdentityService
					.verifyTentativeDevice(userNumber, addTentativeDeviceResponse.addedTentatively.verificationCode)
					.get();

			LOG.info(verifyTentativeDeviceResponse.name());

			internetIdentityService.exitDeviceRegistrationMode(userNumber);

			internetIdentityService.add(userNumber, device);

			DeviceData[] deviceData = internetIdentityService.lookup(userNumber);

			for (int i = 0; i < deviceData.length; i++)
				LOG.info(deviceData[i].alias);

			IdentityAnchorInfo identityAnchorInfoResponse = internetIdentityService.getAnchorInfo(userNumber).get();

			deviceData = identityAnchorInfoResponse.devices;

			for (int i = 0; i < deviceData.length; i++)
				LOG.info(deviceData[i].alias);

			internetIdentityService.remove(userNumber, device.pubkey);

			deviceData = internetIdentityService.lookup(userNumber);

			for (int i = 0; i < deviceData.length; i++)
				LOG.info(deviceData[i].alias);

			InternetIdentityStats stats = internetIdentityService.stats();
			LOG.info(stats.usersRegistered.toString());
		} catch (Exception e) {
			LOG.error(e.getLocalizedMessage(), e);
			Assertions.fail(e.getMessage());
		}
	}

}
