package com.xoom.inf.ksuid;

import org.apache.commons.codec.DecoderException;

import static org.apache.commons.codec.binary.Hex.decodeHex;
import static org.apache.commons.codec.binary.Hex.encodeHexString;

/**
 * Utility class to encode/decode bytes into hexadecimal strings.
 * <p>
 * Unless otherwise noted, passing a {@code null} argument to a method of this class will return null.
 */
final class Hex {

    private Hex() {
        throw new AssertionError("static utility class");
    }

    /**
     * Converts an array of bytes into a String representing the hexadecimal values of each byte in order.
     * The returned String will be double the length of the passed array, as it takes two characters to represent any given byte.
     *
     * @param bytes bytes to encode
     * @return a string of lower-case hexadecimal characters
     */
    static String hexEncode(final byte[] bytes) {
        return bytes != null ? encodeHexString(bytes) : null;
    }

    /**
     * Converts a string of lower-case hexadecimal characters into an array of bytes of those same values.
     * The returned array will be half the length of the passed string, as it takes two characters to represent any given byte.
     * An exception is thrown if the passed string has an odd number of elements.
     *
     * @param hex hexadecimal characters to decode
     * @return a byte array containing binary data decoded from the supplied string
     * @throws IllegalArgumentException if an odd number length string or illegal characters are supplied
     */
    static byte[] hexDecode(final String hex) {
        try {
            return hex != null ? decodeHex(hex.toCharArray()) : null;
        } catch (final DecoderException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
