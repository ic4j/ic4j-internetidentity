package org.ic4j.internetidentity;

import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;

public class Registered{
    @Name("user_number")
    @Field(Type.NAT64)
	public Long userNumber;				
}
