package server.common.uuid;

public class UUIDHelper {

    public static boolean isValid(String uuid) {
        // this works for UUIDs without hyphens
        if (uuid == null) {
            return false;
        }

        uuid = uuid.replaceAll("-", "");

        if (uuid.length() != 32) {
            return false;
        }

        final char[] chars = uuid.toCharArray();
        for (final int c : chars) {
            if (!((c >= 0x30 && c <= 0x39)
                    || (c >= 0x61 && c <= 0x66)
                    || (c >= 0x41 && c <= 0x46))) {
                // ASCII codes: 0-9, a-f, A-F
                return false;
            }
        }

        return true;
    }

    public static boolean isPlayer(String actorId) {
        return !UUIDHelper.isValid(actorId);
    }
}
