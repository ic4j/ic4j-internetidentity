package org.ic4j.internetidentity;

import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;

public class AddedTentatively {
    @Name("verification_code")
    @Field(Type.TEXT)
	public String verificationCode;	
    
    @Name("device_registration_timeout")
    @Field(Type.NAT64)
	public Long deviceRegistrationTimeout;	    
}
