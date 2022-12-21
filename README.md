## Internet Identity Java Service 

This library is a Java implementation of Dfinity Internet Identity Service. Internet Identity is an authentication service for the Internet Computer.
<a href="https://github.com/dfinity/internet-identity/blob/main/docs/internet-identity-spec.adoc#the-internet-identity-specification">https://github.com/dfinity/internet-identity/blob/main/docs/internet-identity-spec.adoc#the-internet-identity-specification</a>. The library communicates directly with Internet Identity canister. Developers can use this library in any Java application to register new users, add or remove devices and authenticate client applications.

To test Internet Identity Service locally, install local Internet Identity dApp. Then just configure custom Java properties with ii.location and ii.canister values pointing to the local instance.

<a href="https://github.com/dfinity/internet-identity/tree/main/demos/using-dev-build">https://github.com/dfinity/internet-identity/tree/main/demos/using-dev-build</a>

To use Internet Identity Service create InternetIdentityService object. Define your own properties if you want to use local instance of the service. 

```
Security.addProvider(new BouncyCastleProvider());
		
BasicIdentity identity = BasicIdentity.fromPEMFile(Paths.get(pemFile));
		
Properties env = InternetIdentityService.getIIProperties();

String iiLocation = env.getProperty("ii.location");
		
ReplicaTransport transport = ReplicaApacheHttpTransport.create(iiLocation);

Agent agent = new AgentBuilder().transport(transport).identity(identity).build();

InternetIdentityService InternetIdentityService = InternetIdentityService.create(agent, env);

```
Then call Internet Identity Service methods. Combine methods to create flow defined in the specification. 

<a href="https://github.com/dfinity/internet-identity/blob/main/docs/internet-identity-spec.adoc#flows">https://github.com/dfinity/internet-identity/blob/main/docs/internet-identity-spec.adoc#flows</a>

```
byte[] sessionKey = identity.derEncodedPublickey;

CompletableFuture<Challenge> challengeResponse = internetIdentityService.createChallenge();
Challenge challenge = challengeResponse.get();

// convert byte[] to a BufferedImage
BufferedImage captchaImage = internetIdentityService.getCaptchaImage(challenge);

ChallengeResult challengeResult = new ChallengeResult();

challengeResult.challengeKey = challenge.challengeKey;
// set captcha characters from the png image
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


Long userNumber = register.registeredValue.userNumber;

String frontendHostname = "http://0.0.0.0:8000/?canisterId=rdmx6-jaaaa-aaaaa-aaadq-cai&id=renrk-eyaaa-aaaaa-aaada-cai";

CompletableFuture<PrepareDelegationResponse> response = internetIdentityService
					.prepareDelegation(userNumber, frontendHostname, sessionKey, Optional.empty());

PrepareDelegationResponse prepareDelegationResponse = response.get();
GetDelegationResponse getDelegationResponse = internetIdentityService.getDelegation(userNumber,
					frontendHostname, sessionKey, prepareDelegationResponse.timestamp);


Principal principal = internetIdentityService.getPrincipal(userNumber, frontendHostname);

// generate a new device key pair
KeyPair keyPair = InternetIdentityService.generateSessionKey();

device.alias = "Device 2";
device.pubkey = keyPair.getPublic().getEncoded();

Long enterDeviceRegistrationModeResponse = internetIdentityService.enterDeviceRegistrationMode(userNumber)
					.get();

AddTentativeDeviceResponse addTentativeDeviceResponse = internetIdentityService
					.addTentativeDevice(userNumber, device).get();


VerifyTentativeDeviceResponse verifyTentativeDeviceResponse = internetIdentityService
					.verifyTentativeDevice(userNumber, addTentativeDeviceResponse.addedTentatively.verificationCode)
					.get();

internetIdentityService.exitDeviceRegistrationMode(userNumber);

internetIdentityService.add(userNumber, device);

DeviceData[] deviceData = internetIdentityService.lookup(userNumber);

IdentityAnchorInfo identityAnchorInfoResponse = internetIdentityService.getAnchorInfo(userNumber).get();

internetIdentityService.remove(userNumber, device.pubkey);

InternetIdentityStats stats = internetIdentityService.stats();
```


# Downloads / Accessing Binaries

To add Java IC4J Internet Identity Service library to your Java project use Maven or Gradle import from Maven Central.

<a href="https://search.maven.org/artifact/ic4j/ic4j-internetidentity/0.6.18/jar">
https://search.maven.org/artifact/ic4j/ic4j-internetidentity/0.6.18/jar
</a>

```
<dependency>
  <groupId>org.ic4j</groupId>
  <artifactId>ic4j-internetidentity</artifactId>
  <version>0.6.18</version>
</dependency>
```

```
implementation 'org.ic4j:ic4j-internetidentity:0.6.18'
```


# Build

You need JDK 8+ to build IC4J Internet Identity Service.
