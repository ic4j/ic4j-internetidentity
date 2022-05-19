package org.ic4j.internetidentity;

import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;
import org.ic4j.types.Principal;

public class Delegation {
    @Name("pubkey")
    @Field(Type.NAT8)
    public byte[] pubkey;
    @Name("expiration")
    @Field(Type.NAT64)
    public Long timestamp; 
    @Name("targets")
    @Field(Type.PRINCIPAL)
    public Principal[] targets;
}
