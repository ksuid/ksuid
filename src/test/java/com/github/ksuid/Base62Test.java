package com.github.ksuid;

import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.Random;

import static com.github.ksuid.Base62.BASE;
import static com.github.ksuid.Base62.BASE_62_CHARACTERS;
import static com.github.ksuid.Base62.base62Decode;
import static com.github.ksuid.Base62.base62Encode;
import static java.util.stream.IntStream.range;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Theories.class)
public class Base62Test {
    @DataPoint
    public static final Entry<byte[], String> P1 = new SimpleEntry<>(Hex.hexDecode("0669F7EFB5A1CD34B5F99D1154FB6853345C9735"), "ujtsYcgvSTl8PAuAdqWYSMnLOv");
    @DataPoint
    public static final Entry<byte[], String> P2 = new SimpleEntry<>(Hex.hexDecode("08499F6AD16FC62A85C3D9C376D121A5478BF798"), "1BJYSC0hh6mbTwDxT0L5c1SlTjM");
    @DataPoint
    public static final Entry<byte[], String> P3 = new SimpleEntry<>(Hex.hexDecode("066A029C73FC1AA3B2446246D6E89FCD909E8FE8"), "ujzPyRiIAffKhBux4PvQdDqMHY");

    @Test(expected = InvocationTargetException.class)
    public void utilityClass() throws Exception {
        final Constructor<Base62> constructor = Base62.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void base() {
        assertThat(BASE).isEqualTo(BigInteger.valueOf(62));
    }

    @Test
    public void characterSet() {
        assertThat(BASE_62_CHARACTERS).isEqualTo("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray());
    }

    @Test
    public void indexOf() {
        range(0, BASE_62_CHARACTERS.length)
                .forEach(index -> {
                    final char charAtIndex = BASE_62_CHARACTERS[index];
                    assertThat(Base62.indexOf(charAtIndex)).isEqualTo(index);
                });
    }

    @Theory
    public void encodeNoPadding(final Entry<byte[], String> entry) {
        final String s = base62Encode(entry.getKey());
        assertThat(s).isEqualTo(entry.getValue());
    }

    @Theory
    public void encodeWithPadding(final Entry<byte[], String> entry) {
        final String s = base62Encode(entry.getKey(), entry.getValue().length() + 4);
        assertThat(s).isEqualTo("0000" + entry.getValue());
    }

    @Theory
    public void encodeWithSameLengthPadding(final Entry<byte[], String> entry) {
        final String s = base62Encode(entry.getKey(), entry.getValue().length());
        assertThat(s).isEqualTo(entry.getValue());
    }

    @Theory
    public void encodeWithPaddingToSmall(final Entry<byte[], String> entry) {
        final String s = base62Encode(entry.getKey(), entry.getValue().length() - 4);
        assertThat(s).isEqualTo(entry.getValue());
    }

    @Theory
    public void decode(final Entry<byte[], String> entry) {
        final byte[] bytes = base62Decode(entry.getValue());
        assertThat(bytes).isEqualTo(entry.getKey());
    }

    @Theory
    public void decodeWithPadding(final Entry<byte[], String> entry) {
        final byte[] bytes = base62Decode("0000" + entry.getValue());
        assertThat(bytes).isEqualTo(entry.getKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void decodeWithInvalidCharacters() {
        base62Decode("01-AB*ab");
    }

    @Theory
    public void encodeDecode(final Entry<byte[], String> entry) {
        assertThat(base62Decode(base62Encode(entry.getKey()))).isEqualTo(entry.getKey());
        assertThat(base62Decode(base62Encode(entry.getKey(), entry.getValue().length() + 4))).isEqualTo(entry.getKey());
    }
    
    @Test
    public void testBase62EncodeUnsigned() {
        final Random random = new Random();
        random.setSeed(123L);

        final Instant timestamp = Instant.parse("2083-01-27T08:18:32.577Z");
        final Ksuid ksuid = new KsuidGenerator(random).newKsuid(timestamp);
        final byte[] ksuidBytes = ksuid.asBytes();
        assertThat(Base62.base62Encode(ksuidBytes, 27)).isEqualTo("IRIRk6W1GtAY07Hp2RJ1blLjvEo");
    }
}
