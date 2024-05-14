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

import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;

//Extra information about registration status for new devices
public final class DeviceRegistrationInfo {
    // If present, the user has tentatively added a new device. This
    // new device needs to be verified (see relevant endpoint) before
    // 'expiration'.
    @Name("tentative_device")
    @Field(Type.RECORD)
    public Optional<DeviceData> tentativeDevice; 
    
    // The timestamp at which the anchor will turn off registration mode
    // (and the tentative device will be forgotten, if any, and if not verified)
    @Name("expiration")
    @Field(Type.NAT64)
    public Long expiration;    
}
