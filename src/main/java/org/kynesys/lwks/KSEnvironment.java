package org.kynesys.lwks;


import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
public class KSEnvironment {
    private HashMap<String, String> envVar = new HashMap<>();
    private final KSUser realUser;
    @Setter private KSUser privilegedUser;

    public KSEnvironment() {
        this.realUser = new KSUser();
        this.privilegedUser = new KSUser();
    }

    public KSEnvironment(KSUser realUser, KSUser privilegedUser) {
        this.realUser = realUser;
        this.privilegedUser = privilegedUser;
    }

    public KSEnvironment(KSUser realUser, KSUser privilegedUser, HashMap<String, String> envVar) {
        this.realUser = realUser;
        this.privilegedUser = privilegedUser;
        this.envVar = envVar;
    }

    public KSEnvironment(KSUser realUser, KSUser privilegedUser, String[] envVarLines) {
        this.realUser = realUser;
        this.privilegedUser = privilegedUser;
        for (String var : envVarLines) {
            String[] keyValue = var.split("=", 2);
            if (keyValue.length == 2) {
                this.envVar.put(keyValue[0], keyValue[1]);
            } else if (keyValue.length == 1) {
                this.envVar.put(keyValue[0], "");
            } else {
                throw new IllegalArgumentException("Invalid environment variable format: " + var);
            }
        }
    }

}