package com.example.common.util;

import java.util.Locale;

public final class NormalizationUtils {

    private NormalizationUtils() {
    }

    public static String normalizeString(String value) {
        if (value == null) {
            return "";
        }
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]", "");
    }
}
