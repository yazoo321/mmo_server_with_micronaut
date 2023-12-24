package server.common.uuid;

public class UUIDHelper {

    public static boolean isValid(String uuid) {
        // this works for UUIDs without hyphens

        if (uuid == null || uuid.length() != 32) {
            return false;
        }

        final char[] chars = uuid.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            final int c = chars[i];
            if (!((c >= 0x30 && c <= 0x39) || (c >= 0x61 && c <= 0x66) || (c >= 0x41 && c <= 0x46))) {
                // ASCII codes: 0-9, a-f, A-F
                return false;
            }
        }

        return true;
    }

}
