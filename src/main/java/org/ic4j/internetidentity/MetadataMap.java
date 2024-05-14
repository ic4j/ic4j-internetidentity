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
import org.ic4j.candid.annotations.Id;
import org.ic4j.candid.types.Type;

//Map with some variants for the value type.
//Note, due to the Candid mapping this must be a tuple type thus we cannot name the fields `key` and `value`.
public final class MetadataMap {
	@Id(0)
    @Field(Type.TEXT)
    public String key;
	
	@Id(1)
    @Field(Type.VARIANT)
    public MetadataMapValue value;
}
