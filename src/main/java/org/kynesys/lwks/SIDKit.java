package org.kynesys.lwks;


import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class SIDKit {
    public enum SIDType {
        USER_OBJECT, DISPATCH_QUEUE, PERMISSION_DATA, DATABASE_STRUCTURE, RECORD_DATA
    }

    public static String generateSID(SIDType type) {
        // First two characters are the type of the SID
        String sid = type.toString().split("_")[0].substring(0, 1);
        sid += type.toString().split("_")[1].charAt(0);

        // Next 6 characters is time, YYMMDD
        sid += "-" + DateTimeFormatter.ofPattern("yyMMdd").format(OffsetDateTime.now());

        // Next 16 characters is UUID (first 8 chars and last 8 chars of UUID)
        UUID uuid = UUID.randomUUID();
        int length = 6;
        String uuidStr = uuid.toString().toUpperCase().replace("-", "");
        String uuidShortStr = uuidStr.substring(0, length);
        return sid + "-" + uuidShortStr;
    }
}