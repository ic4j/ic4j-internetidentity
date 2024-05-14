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

//The same as `DeviceData` but with the `last_usage` field.
//This field cannot be written, hence the separate type.
public final class DeviceWithUsage {
    @Name("pubkey")
    @Field(Type.NAT8)
    public byte[] pubkey;	
    @Name("alias")
    @Field(Type.TEXT)
    public String alias;
    @Name("credential_id")
    @Field(Type.VEC)
    public Optional<byte[]> credentialId;
    @Name("purpose")
    @Field(Type.VARIANT)
    public Purpose purpose; 
    @Name("key_type")
    @Field(Type.VARIANT)
    public KeyType keyType;    
    @Name("protection")
    @Field(Type.VARIANT)
    public DeviceProtection protection;   
    @Name("origin")
    @Field(Type.TEXT)
    public Optional<String> origin;     
    @Name("last_usage")
    @Field(Type.NAT64)
    public Optional<Long> lastUsage;      
    @Name("metadata")
    @Field(Type.RECORD)
    public Optional<MetadataMap> metadata;   
}
