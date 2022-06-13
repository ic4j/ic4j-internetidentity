/*
 * Copyright 2021 Exilor Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.ic4j.internetidentity;

public class InternetIdentityError extends Error {
	private static final long serialVersionUID = -5686403499138076209L;
	InternetIdentityErrorCode code;

	public InternetIdentityError() {

	}

	public InternetIdentityError(String message) {
		super(message);
	}

	public InternetIdentityError(Throwable cause) {
		super(cause);
	}

	public InternetIdentityError(String message, Throwable cause) {
		super(message, cause);
	}
	
	public InternetIdentityError(InternetIdentityErrorCode code) {
		this.code = code;
	}

	public InternetIdentityError(String message, InternetIdentityErrorCode code) {	
		super(message);
		this.code = code;
	}	
	
	public InternetIdentityErrorCode getCode()
	{
		return this.code;
	}
	
	public enum InternetIdentityErrorCode {
	}

}
