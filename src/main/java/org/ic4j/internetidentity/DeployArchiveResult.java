/*
 * Copyright 2024 Exilor Inc.
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

import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;
import org.ic4j.types.Principal;

public enum DeployArchiveResult {
    // The archive was deployed successfully and the supplied wasm module has been installed. The principal of the archive
    // canister is returned.
	success,
	// Initial archive creation is already in progress.
	creation_in_progress,
	// Archive deployment failed. An error description is returned.
	failed;
	
	@Name("success")
	@Field(Type.PRINCIPAL)	
	public Principal successValue;
	
	@Name("failed")
	@Field(Type.TEXT)	
	public String failedValue;	
}
