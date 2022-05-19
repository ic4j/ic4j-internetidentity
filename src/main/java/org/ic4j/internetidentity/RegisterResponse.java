package org.ic4j.internetidentity;

import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;

public enum RegisterResponse {
	// A new user was successfully registered.
	registered,
	// No more registrations are possible in this instance of the II service canister.
	canister_full,
	// The challenge was not successful.
	bad_challenge;
	
	@Name("registered")
	@Field(Type.RECORD)	
	public Registered registeredValue;
}
