package org.ic4j.internetidentity.test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.ic4j.agent.Agent;
import org.ic4j.agent.AgentBuilder;
import org.ic4j.agent.NonceFactory;
import org.ic4j.agent.ReplicaTransport;
import org.ic4j.agent.http.ReplicaApacheHttpTransport;
import org.ic4j.agent.identity.BasicIdentity;
import org.ic4j.agent.identity.DelegatedIdentity;
import org.ic4j.agent.identity.Identity;
import org.ic4j.agent.replicaapi.SignedDelegation;
import org.ic4j.candid.parser.IDLArgs;
import org.ic4j.candid.parser.IDLValue;
import org.ic4j.candid.pojo.PojoDeserializer;
import org.ic4j.candid.pojo.PojoSerializer;
import org.ic4j.internetidentity.AddTentativeDeviceResponse;
import org.ic4j.internetidentity.AnchorCredentials;
import org.ic4j.internetidentity.Challenge;
import org.ic4j.internetidentity.ChallengeResult;
import org.ic4j.internetidentity.DeviceData;
import org.ic4j.internetidentity.DeviceProtection;
import org.ic4j.internetidentity.DeviceWithUsage;
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
			String iiCanister = env.getProperty("ii.canister");

			LOG.info("II Location :" + iiLocation);
			LOG.info("II Canister :" + iiCanister);

			DeviceData device = new DeviceData();

			device.protection = DeviceProtection.isprotected;

			PojoSerializer pojoSerializer = new PojoSerializer();

			IDLValue idlValue = pojoSerializer.serialize(device);

			PojoDeserializer pojoDeserializer = new PojoDeserializer();

			device = pojoDeserializer.deserialize(idlValue, DeviceData.class);

			ReplicaTransport transport = ReplicaApacheHttpTransport.create(iiLocation);

			KeyPair keyPair = InternetIdentityService.generateSessionKey();
			Identity identity = BasicIdentity.fromKeyPair(keyPair);

			//Identity identity = BasicIdentity.fromPEMFile(Paths.get("identity.pem"));

			Agent agent = new AgentBuilder().transport(transport).identity(identity).build();


			agent.fetchRootKey();			
			
			InternetIdentityService internetIdentityService = InternetIdentityService.create(agent, env);
			
			InternetIdentityStats stats = internetIdentityService.stats();
			LOG.info(stats.usersRegistered.toString());

			byte[] sessionKey = keyPair.getPublic().getEncoded();

			CompletableFuture<Challenge> challengeResponse = internetIdentityService.createChallenge();

			Challenge challenge = challengeResponse.get();

			// LOG.info(challenge.pngBase64);
			LOG.info(challenge.challengeKey);

			Path captchaFile = Paths.get("challenge.png");
			// convert byte[] back to a BufferedImage
			BufferedImage captchaImage = internetIdentityService.getCaptchaImage(challenge);

			ChallengeResult challengeResult = new ChallengeResult();

			challengeResult.challengeKey = challenge.challengeKey;
			challengeResult.chars = "a";

			device = new DeviceData();

			device.alias = "Device 1";
			device.pubkey = sessionKey;

			Purpose purpose = Purpose.authentication;

			device.purpose = purpose;

			KeyType keyType = KeyType.platform;
			device.keyType = keyType;

			device.protection = DeviceProtection.isunprotected;

			device.credentialId = Optional.empty();


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
			
			this.testDelegatedIdentity(identity, getDelegationResponse.signedDelegation);

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

			DeviceWithUsage[] deviceDataWithUsage = identityAnchorInfoResponse.devices;

			for (int i = 0; i < deviceDataWithUsage.length; i++)
				LOG.info(deviceDataWithUsage[i].alias);

			internetIdentityService.remove(userNumber, device.pubkey);

			deviceData = internetIdentityService.lookup(userNumber);
			

			for (int i = 0; i < deviceData.length; i++)
				LOG.info(deviceData[i].alias);
			
			//internetIdentityService.getAnchorInfo(userNumber);
			
			AnchorCredentials anchorCredentials = internetIdentityService.getAnchorCredentials(userNumber);

			stats = internetIdentityService.stats();
			LOG.info(stats.usersRegistered.toString());
			

		} catch (Exception e) {
			LOG.error(e.getLocalizedMessage(), e);
			Assertions.fail(e.getMessage());
		}
	}
	
	void testDelegatedIdentity(Identity signingIdentity, SignedDelegation signedDelegation) throws NoSuchAlgorithmException, URISyntaxException, IOException
	{
		InputStream propInputStream = InternetIdenityTest.class.getClassLoader()
				.getResourceAsStream(PROPERTIES_FILE_NAME);

		Properties env = new Properties();
		env.load(propInputStream);

		String iiLocation = env.getProperty("ii.location");

		LOG.info(iiLocation);
		
		String canisterId = env.getProperty("ic.canister");
		
		ReplicaTransport transport = ReplicaApacheHttpTransport.create(iiLocation);
		
		
		List<SignedDelegation> chain = new LinkedList<SignedDelegation>();
		
		chain.add(signedDelegation);
		
		Identity delegatedIdentity = new DelegatedIdentity(signingIdentity , signingIdentity.getPublicKey(), chain);
		
		Agent agent = new AgentBuilder().transport(transport).identity(delegatedIdentity).nonceFactory(new NonceFactory()).ingresExpiry(Duration.ofMinutes(4))
				.build();
		

		agent.fetchRootKey();
		
		
			List<IDLValue> args = new ArrayList<IDLValue>();


			IDLArgs idlArgs = IDLArgs.create(args);

			byte[] buf = idlArgs.toBytes();

			CompletableFuture<byte[]> queryResponse = agent.queryRaw(
					Principal.fromString(canisterId),
					Principal.fromString(canisterId), "greet", buf, Optional.empty(), true);

			try {
				byte[] queryOutput = queryResponse.get();

				IDLArgs outArgs = IDLArgs.fromBytes(queryOutput);

				LOG.info(outArgs.getArgs().get(0).getValue());
				
				Assertions.assertEquals("Hello, " + delegatedIdentity.sender().toString() + "!", outArgs.getArgs().get(0).getValue());
			} catch (Throwable ex) {
				LOG.debug(ex.getLocalizedMessage(), ex);
				Assertions.fail(ex.getLocalizedMessage());
			}
		
	}

}
