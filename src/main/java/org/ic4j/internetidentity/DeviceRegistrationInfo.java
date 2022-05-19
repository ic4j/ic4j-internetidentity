package org.ic4j.internetidentity;

import java.util.Optional;

import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;

public class DeviceRegistrationInfo {
    @Name("tentative_device")
    @Field(Type.RECORD)
    public Optional<DeviceData> tentativeDevice; 
    
    @Name("timestamp")
    @Field(Type.NAT64)
    public Long timestamp;    
}
