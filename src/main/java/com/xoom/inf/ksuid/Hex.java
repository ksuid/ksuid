package com.xoom.inf.ksuid;

/**
 * Utility class to encode/decode bytes into hexadecimal strings.
 * <p>
 * Unless otherwise noted, passing a {@code null} argument to a method of this class will return null.
 * <p>
 * The code in this class comes from the parseHexBinary and printHexBinary methods of javax.xml.bind.DatatypeConverter.
 * The javax.xml.bind.DatatypeConverter is no longer available as of Java 9 so we don't use it for future compatibility.
 */
final class Hex {
    // VisibleForTesting
    static final char[] HEX_CHARACTERS = "0123456789abcdef".toCharArray();

    private Hex() {
        throw new AssertionError("static utility class");
    }

    /**
     * Converts a string of lower-case hexadecimal characters into an array of bytes of those same values.
     * The returned array will be half the length of the passed string, as it takes two characters to represent any given byte.
     * An exception is thrown if the passed string has an odd number of elements or non-hexadecimal characters.
     *
     * @param hex hexadecimal characters to decode
     * @return a byte array containing binary data decoded from the supplied string
     * @throws IllegalArgumentException if an odd number length string or illegal characters are supplied
     */
    static byte[] hexDecode(final String hex) {
        return hex != null ? parseHexBinary(hex) : null;
    }

    /**
     * Converts an array of bytes into a string representing the hexadecimal values of each byte in order.
     * The returned string will be double the length of the passed array, as it takes two characters to represent any given byte.
     *
     * @param bytes bytes to encode
     * @return a string of lower-case hexadecimal characters
     */
    static String hexEncode(final byte[] bytes) {
        return bytes != null ? printHexBinary(bytes) : null;
    }


    private static String printHexBinary(final byte[] bytes) {
        final StringBuilder r = new StringBuilder(bytes.length * 2);
        for (final byte b : bytes) {
            r.append(HEX_CHARACTERS[(b >> 4) & 0xF]);
            r.append(HEX_CHARACTERS[(b & 0xF)]);
        }
        return r.toString();
    }

    private static byte[] parseHexBinary(final String hex) {
        final int len = hex.length();

        // "111" is not a valid hex encoding.
        if (len % 2 != 0) {
            throw new IllegalArgumentException("hex string needs to be even-length: " + hex);
        }

        final byte[] out = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            final int h = hexToBin(hex.charAt(i));
            final int l = hexToBin(hex.charAt(i + 1));
            if (h == -1 || l == -1) {
                throw new IllegalArgumentException("contains illegal character for hex: " + hex);
            }

            out[i / 2] = (byte) (h * 16 + l);
        }

        return out;
    }

    private static int hexToBin(final char ch) {
        if ('0' <= ch && ch <= '9') {
            return ch - '0';
        }
        if ('A' <= ch && ch <= 'F') {
            return ch - 'A' + 10;
        }
        if ('a' <= ch && ch <= 'f') {
            return ch - 'a' + 10;
        }
        return -1;
    }

}
