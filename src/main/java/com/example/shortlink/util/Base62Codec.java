package com.example.shortlink.util;

public final class Base62Codec {

    private static final char[] ALPHABET =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final int BASE = ALPHABET.length;

    private Base62Codec() {
    }

    public static String encode(long value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Base62 value must be positive");
        }

        StringBuilder result = new StringBuilder(11);
        while (value > 0) {
            result.append(ALPHABET[(int) (value % BASE)]);
            value /= BASE;
        }
        return result.reverse().toString();
    }

    public static long decode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Base62 code must not be blank");
        }

        long value = 0;
        for (char current : code.toCharArray()) {
            int digit = indexOf(current);
            if (digit < 0) {
                throw new IllegalArgumentException("Invalid Base62 character: " + current);
            }
            value = Math.addExact(Math.multiplyExact(value, BASE), digit);
        }
        return value;
    }

    private static int indexOf(char current) {
        if (current >= '0' && current <= '9') {
            return current - '0';
        }
        if (current >= 'a' && current <= 'z') {
            return current - 'a' + 10;
        }
        if (current >= 'A' && current <= 'Z') {
            return current - 'A' + 36;
        }
        return -1;
    }
}
