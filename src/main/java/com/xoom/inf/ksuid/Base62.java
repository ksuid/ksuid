package com.xoom.inf.ksuid;

import java.math.BigInteger;
import java.util.stream.IntStream;

import static java.lang.Math.abs;
import static java.lang.Math.ceil;
import static java.lang.Math.log;
import static java.math.BigInteger.ZERO;
import static java.math.BigInteger.valueOf;

/**
 * Utility class to encode/decode bytes into Base62 strings in the same form
 * as <a href="https://github.com/segmentio/ksuid/blob/master/base62.go">https://github.com/segmentio/ksuid/blob/master/base62.go</a>
 * <p>
 * Unless otherwise noted, passing a {@code null} argument to a method of this class
 * will cause a {@link java.lang.NullPointerException NullPointerException} to be thrown.
 * <p>
 * See <a href="https://github.com/segmentio/ksuid">https://github.com/segmentio/ksuid</a>.
 */
final class Base62 {
    // VisibleForTesting
    static final char[] BASE_62_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    // VisibleForTesting
    static final BigInteger BASE = valueOf(BASE_62_CHARACTERS.length);

    private static final int BYTE_BITS = 8;
    private static final double DIGIT_BITS = log(BASE_62_CHARACTERS.length) / log(2);

    private Base62() {
        throw new AssertionError("static utility class");
    }

    /**
     * Encode bytes to Base62 string.
     *
     * @param bytes bytes to encode
     * @return a Base62 string
     */
    static String base62Encode(final byte[] bytes) {
        return base62Encode(bytes, 0);
    }

    /**
     * Encode bytes to Base62 string.
     * <p>
     * If length is greater than the size of the Base62 string,
     * the returned string will be padded with zeros up to length size.
     * <p>
     * See <a href="https://stackoverflow.com/questions/14110010/base-n-encoding-of-a-byte-array">stack overflow comments</a>
     * for the inspiration of this method.
     *
     * @param bytes bytes to encode
     * @param length length to which to pad the returned string with zeros
     * @return a Base62 string
     */
    static String base62Encode(final byte[] bytes, final int length) {
        final int size = (int) ceil((bytes.length * BYTE_BITS) / DIGIT_BITS);
        final StringBuilder sb = new StringBuilder(size);

        BigInteger value = new BigInteger(bytes);
        while (value.compareTo(ZERO) > 0) {
            final BigInteger[] quotientAndRemainder = value.divideAndRemainder(BASE);
            sb.append(BASE_62_CHARACTERS[abs(quotientAndRemainder[1].intValue())]);
            value = quotientAndRemainder[0];
        }

        while (length > 0 && sb.length() < length) {
            sb.append('0');
        }

        return sb.reverse().toString();
    }

    /**
     * Decode a Base62 string into bytes.
     *
     * @param s base62 string to decode
     * @return decoded bytes
     */
    static byte[] base62Decode(final String s) {
        return IntStream.range(0, s.length())
                        .mapToObj(s::charAt)
                        .map(Base62::indexOf)
                        .map(BigInteger::valueOf)
                        .reduce(ZERO, (result, index) -> result.multiply(BASE).add(index))
                        .toByteArray();
    }

    // VisibleForTesting
    static int indexOf(final char c) {
        if (c >= '0' && c <= '9') {
            return (c - 48);
        }
        if (c >= 'A' && c <= 'Z') {
            return (c - 55);
        }
        if (c >= 'a' && c <= 'z') {
            return (c - 61);
        }
        throw new IllegalArgumentException("'" + c + "' is not a valid Base62 character");
    }

}
