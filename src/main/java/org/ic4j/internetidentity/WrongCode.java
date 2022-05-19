package org.ic4j.internetidentity;

import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;

public class WrongCode {
    @Name("retries_left")
    @Field(Type.NAT8)
	public Short retriesLeft;		
}
