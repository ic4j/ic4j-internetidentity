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

import org.ic4j.agent.replicaapi.SignedDelegation;
import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;

public enum GetDelegationResponse {
	// The signed delegation was successfully retrieved.
	signed_delegation,
	// The signature is not ready. Maybe retry by calling `prepare_delegation`
	no_such_delegation;

	@Name("signed_delegation")
	@Field(Type.RECORD)
	public SignedDelegation signedDelegation;
}
