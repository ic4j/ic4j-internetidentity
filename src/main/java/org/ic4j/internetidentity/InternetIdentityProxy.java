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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.ic4j.agent.annotations.Waiter;
import org.ic4j.agent.annotations.UPDATE;
import org.ic4j.agent.annotations.Argument;
import org.ic4j.agent.annotations.QUERY;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.parser.IDLArgs;
import org.ic4j.candid.types.Type;
import org.ic4j.types.Principal;


public interface InternetIdentityProxy {
	
	@UPDATE
	@Name("init_salt")
	@Waiter(timeout = 30)
	public void initSalt();
	
	@UPDATE
	@Name("create_challenge")
	@Waiter(timeout = 30)
	public CompletableFuture<Challenge> createChallenge();
	
	@UPDATE
	@Name("register")
	@Waiter(timeout = 30)
	public CompletableFuture<RegisterResponse> register(@Argument(Type.RECORD)DeviceData deviceData, @Argument(Type.RECORD)ChallengeResult challengeResult);	

	@UPDATE
	@Name("add")
	@Waiter(timeout = 30)
	public void add(@Argument(Type.NAT64)Long userNumber, @Argument(Type.RECORD)DeviceData deviceData);
	
	@UPDATE
	@Name("update")
	@Waiter(timeout = 30)
	public void update(@Argument(Type.NAT64)Long userNumber, @Argument(Type.NAT8)byte[] deviceKey, @Argument(Type.RECORD)DeviceData deviceData);	

	@UPDATE
	@Name("remove")
	@Waiter(timeout = 30)
	public void remove(@Argument(Type.NAT64)Long userNumber, @Argument(Type.NAT8)byte[] deviceKey);
	
	// Returns all devices of the user (authentication and recovery) but no information about device registrations.
	// Note: Will be changed in the future to be more consistent with get_anchor_info.
	@QUERY
	@Name("lookup")
	public DeviceData[] lookup(@Argument(Type.NAT64)Long userNumber);
	
	@UPDATE
	@Name("get_anchor_info")
	@Waiter(timeout = 30)
	public CompletableFuture<IdentityAnchorInfo> getAnchorInfo(@Argument(Type.NAT64)Long userNumber);	

	@QUERY
	@Name("get_principal")
	public Principal getPrincipal(@Argument(Type.NAT64)Long userNumber,@Argument(Type.TEXT)String frontendHostname);
	
	@QUERY
	@Name("stats")
	public InternetIdentityStats stats();	

	@UPDATE
	@Name("enter_device_registration_mode")
	@Waiter(timeout = 30)
	public CompletableFuture<Long> enterDeviceRegistrationMode(@Argument(Type.NAT64)Long userNumber);	
	
	@UPDATE
	@Name("exit_device_registration_mode")
	@Waiter(timeout = 30)
	public void exitDeviceRegistrationMode(@Argument(Type.NAT64)Long userNumber);	
	
	@UPDATE
	@Name("add_tentative_device")
	@Waiter(timeout = 30)
	public CompletableFuture<AddTentativeDeviceResponse> addTentativeDevice(@Argument(Type.NAT64)Long userNumber,  @Argument(Type.RECORD)DeviceData deviceData);
	
	@UPDATE
	@Name("verify_tentative_device")
	@Waiter(timeout = 30)
	public CompletableFuture<VerifyTentativeDeviceResponse> verifyTentativeDevice(@Argument(Type.NAT64)Long userNumber, @Argument(Type.TEXT)String verificationCode);	


	@UPDATE
	@Name("prepare_delegation")
	@Waiter(timeout = 30)	
	public CompletableFuture<IDLArgs> prepareDelegation(@Argument(Type.NAT64)Long userNumber,  @Argument(Type.TEXT)String frontendHostname,
			 @Argument(Type.NAT8)byte[] sessionKey, @Argument(Type.OPT)Optional<Long> maxTimeToLive);
	
	@QUERY
	@Name("get_delegation")
	public GetDelegationResponse getDelegation(@Argument(Type.NAT64)Long userNumber,@Argument(Type.TEXT)String frontendHostname,  @Argument(Type.NAT8)byte[] sessionKey, @Argument(Type.NAT64)Long timestamp);
	
}
