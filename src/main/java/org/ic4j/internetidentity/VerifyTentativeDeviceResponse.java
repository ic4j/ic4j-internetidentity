package org.ic4j.internetidentity;


import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;

public enum VerifyTentativeDeviceResponse {
	// The device was successfully verified.
	verified,
	// Wrong verification code entered. Retry with correct code.
	wrong_code,	
	// Device registration mode is off, either due to timeout or because it was never enabled.
	device_registration_mode_off,
	  // There is no tentative device to be verified.
	no_device_to_verify;

	@Name("wrong_code")
	@Field(Type.RECORD)
	public WrongCode wrongCode;
}
