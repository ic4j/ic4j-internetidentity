package org.ic4j.internetidentity;

import java.util.Map;

import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Label;
import org.ic4j.candid.types.Type;

public class InternetIdentityStats {
    @Name("users_registered")
    @Field(Type.NAT64)
    public Long usersRegistered;
    @Name("assigned_user_number_range")
    @Field(Type.RECORD)
    public Map<Label,Object> assignedUserNumberRange;    
}
