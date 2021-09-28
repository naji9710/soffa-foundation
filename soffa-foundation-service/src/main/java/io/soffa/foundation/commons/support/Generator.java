package io.soffa.foundation.commons.support;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.util.UUID;

public final class Generator {

    private Generator() {
    }

    public static String shortId() {
        return shortId("");
    }

    public static String shortId(String prefix) {
        UUID uuid = UUID.randomUUID();
        long l = ByteBuffer.wrap(uuid.toString().getBytes()).getLong();
        return prefix(Long.toString(l, Character.MAX_RADIX), prefix);
    }

    public static  String secureRandomId()  {
        return secureRandomId("");
    }

    public static  String secureRandomId(String prefix)  {
        return prefix(NanoIdUtils.randomNanoId(), prefix);
    }

    private static String prefix(String value, String prefix) {
        if (StringUtils.isEmpty(prefix)) {
            return value;
        }
        String  prefix2 = prefix.trim().toLowerCase();
        boolean hasNoDelimier = prefix2.matches(".*[a-zA-Z]$");
        if (hasNoDelimier) { // no delimiter found
            return prefix2 + "_" + value;
        }
        return prefix2 + value;
    }


}
