package org.ic4j.internetidentity;

import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;

public enum AddTentativeDeviceResponse {
	// The device was tentatively added.
	added_tentatively,
	// Device registration mode is off, either due to timeout or because it was never enabled.
	device_registration_mode_off,	
	// There is another device already added tentatively
	another_device_tentatively_added;

	@Name("added_tentatively")
	@Field(Type.RECORD)
	public AddedTentatively addedTentatively;
}
