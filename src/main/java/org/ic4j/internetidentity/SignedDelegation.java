package org.ic4j.internetidentity;

import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;

public class SignedDelegation {
    @Name("delegation")
    @Field(Type.RECORD)
    public Delegation delegation;	
    @Name("signature")
    @Field(Type.NAT8)
    public byte[] signature;

}
