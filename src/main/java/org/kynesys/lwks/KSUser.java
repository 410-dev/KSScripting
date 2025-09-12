package org.kynesys.lwks;

import lombok.Getter;

@Getter
public class KSUser {

    public static final String DefaultUserName = "default-user";
    public static final String DefaultUserDisplayName = "Default User";

    private final String SID;
    private final String username;
    private final String userDisplayName;

    public KSUser() {
        this.SID = SIDKit.generateSID(SIDKit.SIDType.USER_OBJECT);
        this.username = DefaultUserName;
        this.userDisplayName = DefaultUserDisplayName;
    }
}