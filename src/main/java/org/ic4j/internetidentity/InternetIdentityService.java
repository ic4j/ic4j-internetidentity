/*
 * Copyright 2021 Exilor Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

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
import java.util.Base64;
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
import org.ic4j.candid.parser.IDLArgs;
import org.ic4j.types.Principal;

public final class InternetIdentityService {
	static final String PROPERTIES_FILE_NAME = "internetidentity.properties";

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

	public static Properties getIIProperties() {
		return env;
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

	/*
	 * Fetches all device data associated with a user.
	 * 
	 * Authorization: Anyone can call this
	 */
	public DeviceData[] lookup(Long userNumber) {
		return internetIdentityProxy.lookup(userNumber);
	}
	
	public AnchorCredentials getAnchorCredentials(Long userNumber) {
		return internetIdentityProxy.getAnchorCredentials(userNumber);
	}	

	public InternetIdentityStats stats() {
		return internetIdentityProxy.stats();
	}

	/*
	 * In order to protect the Internet Computer from too many “free” update calls,
	 * and to protect the Internet Identity Service from too many user
	 * registrations, this call is protected using a CAPTCHA challenge. The register
	 * call can only succeed if the ChallengeResult contains a key for a challenge
	 * that was created with create_challenge (see below) in the last 5 minutes and
	 * if the chars match the characters that the Internet Identity Service has
	 * stored internally for that key. 
	 * 
	 * Authorization: This request must be sent to the canister with caller that is
	 * the self-authenticating id derived from the given DeviceKey.
	 */
	 
	public CompletableFuture<Challenge> createChallenge() {
		return internetIdentityProxy.createChallenge();
	}

	
	/*
	 * The register method is used to create a new user. The Internet Identity
	 * Service backend creates a fresh Identity Anchor, creates the account record,
	 * and adds the given device as the first device.
	 * 
	 * Authorization: This request must be sent to the canister with caller that is
	 * the self-authenticating id derived from the given DeviceKey.
	 */	 
	public CompletableFuture<RegisterResponse> register(DeviceData deviceData, ChallengeResult challengeResult) {
		return internetIdentityProxy.register(deviceData, challengeResult);
	}

	/*
	 * The prepare_delegation method causes the Internet Identity Service backend to
	 * prepare a delegation from the user identity associated with the given
	 * Identity Anchor and Client Application Frontend Hostname to the given session
	 * key.
	 * 
	 * This method returns the user’s identity that’s associated with the given
	 * Client Application Frontend Hostname. By returning this here, and not in the
	 * less secure get_delegation query, we prevent attacks that trick the user into
	 * using a wrong identity.
	 * 
	 * The expiration timestamp is determined by the backend, but no more than
	 * maxTimeToLive (if present) nanoseconds in the future.
	 * 
	 * The method returns the expiration timestamp of the delegation. This is
	 * returned purely so that the client can feed it back to the backend in
	 * get_delegation.
	 * 
	 * The actual delegation can be fetched using get_delegation immediately
	 * afterwards.
	 * 
	 * Authorization: This request must be sent to the canister with caller that is
	 * the self-authenticating id derived from any of the public keys of devices
	 * associated with the user before this call.
	 */	
	
	public CompletableFuture<PrepareDelegationResponse> prepareDelegation(Long userNumber, String frontendHostname,
			byte[] sessionKey, Optional<Long> maxTimeToLive) {
		CompletableFuture<PrepareDelegationResponse> response = new CompletableFuture<PrepareDelegationResponse>();

		try {

			IDLArgs outArgs = internetIdentityProxy.prepareDelegation(userNumber, frontendHostname, sessionKey, maxTimeToLive).get();
			PrepareDelegationResponse prepareDelegationResponse = new PrepareDelegationResponse();

			prepareDelegationResponse.userKey = ArrayUtils.toPrimitive((Byte[]) outArgs.getArgs().get(0).getValue());
			prepareDelegationResponse.timestamp = outArgs.getArgs().get(1).getValue();

			response.complete(prepareDelegationResponse);

		} catch (InterruptedException | ExecutionException e) {
			response.completeExceptionally(new InternetIdentityError(e));
		}

		return response;
	}

	/*
	 * For a certain amount of time after a call to prepare_delegation, a query call
	 * to get_delegation with the same arguments, plus the timestamp returned from
	 * prepare_delegation, actually fetches the delegation.
	 * 
	 * Together with the UserKey returned by prepare_delegation, the result of this
	 * method is used by the Frontend to pass to the client application as per the
	 * client authentication protocol.
	 * 
	 * Authorization: This request must be sent to the canister with caller that is
	 * the self-authenticating id derived from any of the public keys of devices
	 * associated with the user before this call.
	 */	
	public GetDelegationResponse getDelegation(Long userNumber, String frontendHostname, byte[] sessionKey,
			Long timestamp) throws InternetIdentityError {

		return internetIdentityProxy.getDelegation(userNumber, frontendHostname, sessionKey, timestamp);
	}
	
	/*
	 * Enables device registration mode for the given identity anchor. When device
	 * registration mode is active, new devices can be added using
	 * add_tentative_device and verify_tentative_device. Device registration mode
	 * stays active for at most 15 minutes or until the flow is either completed or
	 * aborted.
	 * 
	 * Authorization: This request must be sent to the canister with caller that is
	 * the self-authenticating id derived from any of the public keys of devices
	 * associated with the user before this call.
	 */
	public CompletableFuture<Long> enterDeviceRegistrationMode(Long userNumber)
	{
		return internetIdentityProxy.enterDeviceRegistrationMode(userNumber);
	}
	
	/*
	 * Exits device registration mode immediately. Any non verified tentative
	 * devices are discarded.
	 * 
	 * Authorization: This request must be sent to the canister with caller that is
	 * the self-authenticating id derived from any of the public keys of devices
	 * associated with the user before this call.
	 */	
	public void exitDeviceRegistrationMode(Long userNumber)
	{
		internetIdentityProxy.exitDeviceRegistrationMode(userNumber);
	}

	/*
	 * Tentatively adds a new device to the supplied identity anchor and returns a
	 * verification code. This code has to be used with the verify_tentative_device
	 * method to verify this device. If the flow is aborted or not completed within
	 * 15 minutes, the tentative device is discarded.
	 * 
	 * Tentatively added devices cannot be used to login into the management view or
	 * authorize authentications for other dApps.
	 * 
	 * Authorization: Anyone can call this
	 */
	public CompletableFuture<AddTentativeDeviceResponse> addTentativeDevice(Long userNumber, DeviceData deviceData) {

		return internetIdentityProxy.addTentativeDevice(userNumber, deviceData);
	}

	/*
	 * For an anchor in device registration mode: if called with a valid
	 * verification code, adds the tentative device as a regular device to the
	 * anchor and exits registration mode. The registration flow is aborted if this
	 * method is called five times with invalid verification codes.
	 * 
	 * Returns an error if called for a device not in registration mode.
	 * 
	 * Authorization: This request must be sent to the canister with caller that is
	 * the self-authenticating id derived from any of the public keys of devices
	 * associated with the user before this call.
	 */	
	public CompletableFuture<VerifyTentativeDeviceResponse> verifyTentativeDevice(Long userNumber,
			String verificationCode) {
		return internetIdentityProxy.verifyTentativeDevice(userNumber, verificationCode);
	}

	/*
	 * The add method appends a new device to the given user’s record.
	 * 
	 * The Internet Identity Service backend rejects the call if the user already
	 * has a device on record with the given public key.
	 * 
	 * This may also fail (with a reject) if the user is registering too many
	 * devices.
	 * 
	 * Authorization: This request must be sent to the canister with caller that is
	 * the self-authenticating id derived from any of the public keys of devices
	 * associated with the user before this call.
	 */
	public void add(Long userNumber, DeviceData deviceData) {
		internetIdentityProxy.add(userNumber, deviceData);
	}

	/*
	 * The remove method removes a device, identified by its public key, from the
	 * list of devices a user has.
	 * 
	 * It is allowed to remove the key that is used to sign this request. This can
	 * be useful for a panic button functionality.
	 * 
	 * It is allowed to remove the last key, to completely disable a user. The
	 * canister may forget that user completely then, assuming the Identity Anchor
	 * generation algorithm prevents new users from getting the same Identity
	 * Anchor.
	 * 
	 * It is the responsibility of the frontend UI to protect the user from doing
	 * these things accidentally.
	 * 
	 * Authorization: This request must be sent to the canister with caller that is
	 * the self-authenticating id derived from any of the public keys of devices
	 * associated with the user before this call.
	 */	
	public void remove(Long userNumber, byte[] deviceKey) {
		internetIdentityProxy.remove(userNumber, deviceKey);
	}
	
	public void update(Long userNumber, byte[] deviceKey, DeviceData deviceData) {
		internetIdentityProxy.update(userNumber, deviceKey,deviceData);
	}	

	/*
	 * Fetches the principal for a given user and front end.
	 * 
	 * Authorization: This request must be sent to the canister with caller that is
	 * the self-authenticating id derived from any of the public keys of devices
	 * associated with the user before this call.
	 */	
	public Principal getPrincipal(Long userNumber, String frontendHostname) {
		return internetIdentityProxy.getPrincipal(userNumber, frontendHostname);
	}

	/*
	 * Fetches all data associated with an anchor including registration mode and
	 * tentatively registered devices.
	 * 
	 * Authorization: This request must be sent to the canister with caller that is
	 * the self-authenticating id derived from any of the public keys of devices
	 * associated with the user before this call.
	 */
	public CompletableFuture<IdentityAnchorInfo> getAnchorInfo(Long userNumber) {
		return internetIdentityProxy.getAnchorInfo(userNumber);
	}

	public CompletableFuture<DeployArchiveResult> deployArchive(byte[] wasm) {
		return internetIdentityProxy.deployArchive(wasm);
	}
	
	public void acknowledgeEntries(Long sequenceNumber) {
		internetIdentityProxy.acknowledgeEntries(sequenceNumber);
	}
}
