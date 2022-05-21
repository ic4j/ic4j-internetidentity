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

import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;

public enum AddTentativeDeviceResponse {
	// The device was tentatively added.
	added_tentatively,
	// Device registration mode is off, either due to timeout or because it was never enabled.
	device_registration_mode_off,	
	// There is another device already added tentatively
	another_device_tentatively_added;

	@Name("added_tentatively")
	@Field(Type.RECORD)
	public AddedTentatively addedTentatively;
}
