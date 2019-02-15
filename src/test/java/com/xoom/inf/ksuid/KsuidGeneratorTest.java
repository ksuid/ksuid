package com.xoom.inf.ksuid;

import org.junit.Rule;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.security.SecureRandom;
import java.time.Instant;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(Theories.class)
public class KsuidGeneratorTest {

    @DataPoints
    public static final int[] INCORRECT_SIZES = {0, 10, 25}; // anything but 16

    @DataPoints
    public static final KsuidGenerator[] GENERATORS = {
            new KsuidGenerator(new SecureRandom()),
            new KsuidGenerator(() -> new byte[16])
    };

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Theory
    public void newKsuid(final KsuidGenerator generator) {
        Ksuid ksuid = generator.newKsuid();
        assertThat(ksuid, is(notNullValue()));
        assertThat(ksuid.getInstant().isBefore(now()), is(true));

        final Instant instant = now();
        ksuid = generator.newKsuid(instant);
        assertThat(ksuid, is(notNullValue()));
        assertThat(ksuid.getInstant(), is(instant.truncatedTo(SECONDS)));
    }

    @Theory
    public void constructWithSupplierOfIncorrectSize(final int incorrectSize) {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("payloadBytesSupplier must supply byte arrays of length 16");
        new KsuidGenerator(() -> new byte[incorrectSize]);
    }

}
