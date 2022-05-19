package org.ic4j.internetidentity;

import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;

public class Challenge {
    @Name("png_base64")
    @Field(Type.TEXT)
    public String pngBase64;
    @Name("challenge_key")
    @Field(Type.TEXT)
    public String challengeKey;    
}
