package org.ic4j.internetidentity;

import org.ic4j.candid.annotations.Field;
import org.ic4j.candid.annotations.Name;
import org.ic4j.candid.types.Type;

public class ChallengeResult {
    @Name("key")
    @Field(Type.TEXT)
    public String challengeKey;   
    @Name("chars")
    @Field(Type.TEXT)
    public String chars;    
}
