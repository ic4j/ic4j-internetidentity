package org.ic4j.internetidentity;

import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;

public enum GetDelegationResponse {
	// The signed delegation was successfully retrieved.
	signed_delegation,
	// The signature is not ready. Maybe retry by calling `prepare_delegation`
	no_such_delegation;

	@Name("signed_delegation")
	@Field(Type.RECORD)
	public SignedDelegation signedDelegation;
}
