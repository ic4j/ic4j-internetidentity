package org.ic4j.internetidentity;

import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;

public class IdentityAnchorInfo {
    @Name("devices")
    @Field(Type.RECORD)
    public DeviceData[] devices;   
    @Name("device_registration")
    @Field(Type.RECORD)
    public DeviceRegistrationInfo deviceRegistration;    
}
