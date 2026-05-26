package com.xj.agri.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public final class PasswordUtil {
    private static final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder(10);

    private PasswordUtil() {
    }

    public static String encode(String raw) {
        return BCRYPT.encode(raw);
    }

    public static boolean matches(String raw, String encoded) {
        return encoded != null && BCRYPT.matches(raw, encoded);
    }
}
