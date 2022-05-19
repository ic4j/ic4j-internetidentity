package org.ic4j.internetidentity;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.ic4j.agent.Agent;
import org.ic4j.agent.ProxyBuilder;
import org.ic4j.agent.UpdateBuilder;
import org.ic4j.agent.Waiter;
import org.ic4j.candid.parser.IDLArgs;
import org.ic4j.candid.parser.IDLType;
import org.ic4j.candid.parser.IDLValue;
import org.ic4j.candid.types.Type;
import org.ic4j.types.Principal;

public class InternetIdentityService {
	static final String PROPERTIES_FILE_NAME = "internetidentity.properties";
	static final String PREPARE_DELEGATION_METHOD_NAME = "prepare_delegation";

	static Properties env;

	String iiCanister;

	Agent agent;

	InternetIdentityProxy internetIdentityProxy;

	static {
		InputStream propInputStream = InternetIdentityService.class.getClassLoader()
				.getResourceAsStream(PROPERTIES_FILE_NAME);

		env = new Properties();
		try {
			env.load(propInputStream);

		} catch (IOException e) {
			throw new InternetIdentityError(e);
		}
	}

	public static InternetIdentityService create(Agent agent) throws InternetIdentityError {
		return create(agent, env);
	}

	public static InternetIdentityService create(Agent agent, Properties env) throws InternetIdentityError {

		InternetIdentityService internetIdentityService = new InternetIdentityService();

		internetIdentityService.iiCanister = env.getProperty("ii.canister");

		internetIdentityService.agent = agent;

		internetIdentityService.internetIdentityProxy = ProxyBuilder
				.create(agent, Principal.fromString(internetIdentityService.iiCanister))
				.getProxy(InternetIdentityProxy.class);

		return internetIdentityService;
	}

	public static String getIILocation() {
		return env.getProperty("ii.location");
	}

	public static KeyPair generateSessionKey() throws NoSuchAlgorithmException {
		return KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
	}

	public static void savePrivateKey(PrivateKey privateKey, String pemFileName) throws IOException {
		PemObject pemObject = new PemObject("PRIVATE KEY", privateKey.getEncoded());

		Path pemFile = Paths.get(pemFileName);

		PemWriter pemWriter = new PemWriter(new FileWriter(pemFile.toFile()));
		pemWriter.writeObject(pemObject);
		pemWriter.close();
	}

	public static BufferedImage getCaptchaImage(Challenge challenge) throws IOException {
		InputStream is = new ByteArrayInputStream(Base64.getDecoder().decode(challenge.pngBase64.getBytes()));
		return ImageIO.read(is);
	}

	public DeviceData[] lookup(Long userNumber) {
		return internetIdentityProxy.lookup(userNumber);
	}

	public InternetIdentityStats stats() {
		return internetIdentityProxy.stats();
	}

	public CompletableFuture<Challenge> createChallenge() {
		return internetIdentityProxy.createChallenge();
	}

	public CompletableFuture<RegisterResponse> register(DeviceData deviceData, ChallengeResult challengeResult) {
		return internetIdentityProxy.register(deviceData, challengeResult);
	}

	public CompletableFuture<PrepareDelegationResponse> prepareDelegation(Long userNumber, String frontendHostname,
			byte[] sessionKey, Optional<Long> maxTimeToLive) {
		CompletableFuture<PrepareDelegationResponse> response = new CompletableFuture<PrepareDelegationResponse>();

		IDLValue userNumberValue = IDLValue.create(userNumber, Type.NAT64);
		IDLValue frontendHostnameValue = IDLValue.create(frontendHostname);
		IDLValue sessionKeyValue = IDLValue.create(sessionKey,
				IDLType.createType(Type.VEC, IDLType.createType(Type.NAT8)));
		IDLValue maxTimeToLiveValue = IDLValue.create(maxTimeToLive);

		List<IDLValue> args = new ArrayList<IDLValue>();

		args.add(userNumberValue);
		args.add(frontendHostnameValue);
		args.add(sessionKeyValue);
		args.add(maxTimeToLiveValue);

		IDLArgs idlArgs = IDLArgs.create(args);

		byte[] buf = idlArgs.toBytes();

		UpdateBuilder updateBuilder = UpdateBuilder
				.create(this.agent, Principal.fromString(this.iiCanister), PREPARE_DELEGATION_METHOD_NAME).arg(buf);

		CompletableFuture<byte[]> builderResponse = updateBuilder.callAndWait(Waiter.create(30, 5));

		byte[] output;
		try {
			output = builderResponse.get();
			IDLArgs outArgs = IDLArgs.fromBytes(output);
			PrepareDelegationResponse prepareDelegationResponse = new PrepareDelegationResponse();

			prepareDelegationResponse.userKey = ArrayUtils.toPrimitive((Byte[]) outArgs.getArgs().get(0).getValue());
			prepareDelegationResponse.timestamp = outArgs.getArgs().get(1).getValue();

			response.complete(prepareDelegationResponse);

		} catch (InterruptedException | ExecutionException e) {
			response.completeExceptionally(new InternetIdentityError(e));
		}

		return response;
	}

	public GetDelegationResponse getDelegation(Long userNumber, String frontendHostname, byte[] sessionKey,
			Long timestamp) throws InternetIdentityError {

		return internetIdentityProxy.getDelegation(userNumber, frontendHostname, sessionKey, timestamp);
	}
	
	public CompletableFuture<Long> enterDeviceRegistrationMode(Long userNumber)
	{
		return internetIdentityProxy.enterDeviceRegistrationMode(userNumber);
	}
	
	public void exitDeviceRegistrationMode(Long userNumber)
	{
		internetIdentityProxy.exitDeviceRegistrationMode(userNumber);
	}

	public CompletableFuture<AddTentativeDeviceResponse> addTentativeDevice(Long userNumber, DeviceData deviceData) {

		return internetIdentityProxy.addTentativeDevice(userNumber, deviceData);
	}

	public CompletableFuture<VerifyTentativeDeviceResponse> verifyTentativeDevice(Long userNumber,
			String verificationCode) {
		return internetIdentityProxy.verifyTentativeDevice(userNumber, verificationCode);
	}

	public void add(Long userNumber, DeviceData deviceData) {
		internetIdentityProxy.add(userNumber, deviceData);
	}

	public void remove(Long userNumber, byte[] deviceKey) {
		internetIdentityProxy.remove(userNumber, deviceKey);
	}

	public Principal getPrincipal(Long userNumber, String frontendHostname) {
		return internetIdentityProxy.getPrincipal(userNumber, frontendHostname);
	}

	public CompletableFuture<IdentityAnchorInfo> getAnchorInfo(Long userNumber) {
		return internetIdentityProxy.getAnchorInfo(userNumber);
	}

}
