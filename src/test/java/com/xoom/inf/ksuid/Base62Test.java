package com.xoom.inf.ksuid;

import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

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
        assertThat(Base62.BASE, is(BigInteger.valueOf(62)));
    }

    @Test
    public void characterSet() {
        assertThat(Base62.BASE_62_CHARACTERS, is("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray()));
    }

    @Test
    public void indexOf() {
        IntStream.range(0, Base62.BASE_62_CHARACTERS.length)
                 .forEach(index -> {
                     final char charAtIndex = Base62.BASE_62_CHARACTERS[index];
                     assertThat(Base62.indexOf(charAtIndex), is(index));
                 });
    }

    @Theory
    public void encodeNoPadding(final Entry<byte[], String> entry) {
        final String s = Base62.base62Encode(entry.getKey());
        assertThat(s, is(entry.getValue()));
    }

    @Theory
    public void encodeWithPadding(final Entry<byte[], String> entry) {
        final String s = Base62.base62Encode(entry.getKey(), entry.getValue().length() + 4);
        assertThat(s, is("0000" + entry.getValue()));
    }

    @Theory
    public void encodeWithSameLengthPadding(final Entry<byte[], String> entry) {
        final String s = Base62.base62Encode(entry.getKey(), entry.getValue().length());
        assertThat(s, is(entry.getValue()));
    }

    @Theory
    public void encodeWithPaddingToSmall(final Entry<byte[], String> entry) {
        final String s = Base62.base62Encode(entry.getKey(), entry.getValue().length() - 4);
        assertThat(s, is(entry.getValue()));
    }

    @Theory
    public void decode(final Entry<byte[], String> entry) {
        final byte[] bytes = Base62.base62Decode(entry.getValue());
        assertThat(bytes, is(entry.getKey()));
    }

    @Theory
    public void decodeWithPadding(final Entry<byte[], String> entry) {
        final byte[] bytes = Base62.base62Decode("0000" + entry.getValue());
        assertThat(bytes, is(entry.getKey()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void decodeWithInvalidCharacters() {
        Base62.base62Decode("01-AB*ab");
    }

    @Theory
    public void encodeDecode(final Entry<byte[], String> entry) {
        byte[] bytes = Base62.base62Decode(Base62.base62Encode(entry.getKey()));
        assertThat(bytes, is(entry.getKey()));

        bytes = Base62.base62Decode(Base62.base62Encode(entry.getKey(), entry.getValue().length() + 4));
        assertThat(bytes, is(entry.getKey()));
    }

}
