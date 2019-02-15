package com.xoom.inf.ksuid;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class HexTest {
    private static final String PLAIN_TEXT = "The quick brown fox jumps over the lazy dog";
    private static final String HEX = "54686520717569636b2062726f776e20666f78206a756d7073206f76657220746865206c617a7920646f67";

    @Test(expected = InvocationTargetException.class)
    public void utilityClass() throws Exception {
        final Constructor<Hex> constructor = Hex.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    @Test
    public void hexDecode() {
        assertThat(Hex.hexDecode(HEX), is(PLAIN_TEXT.getBytes()));
    }

    @Test
    public void hexDecodeNull() {
        assertThat(Hex.hexDecode(null), is(nullValue()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void hexDecodeIllegalHexCharacter() {
        Hex.hexDecode(HEX.replace('5', 'z'));
        fail("Expected " + IllegalArgumentException.class);
    }

    @Test
    public void hexEncode() {
        assertThat(Hex.hexEncode(PLAIN_TEXT.getBytes()), is(HEX));
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void hexEncodeNull() {
        assertThat(Hex.hexEncode(null), is(nullValue()));
    }

}
