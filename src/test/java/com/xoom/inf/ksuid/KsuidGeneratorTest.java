package com.xoom.inf.ksuid;

import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.security.SecureRandom;
import java.time.Instant;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@RunWith(Theories.class)
public class KsuidGeneratorTest {

    @DataPoints
    public static final int[] INCORRECT_SIZES = {0, 10, 25}; // anything but 16

    @DataPoints
    public static final KsuidGenerator[] GENERATORS = {
            new KsuidGenerator(new SecureRandom()),
            new KsuidGenerator(() -> new byte[16])
    };

    @Theory
    public void newKsuid(final KsuidGenerator generator) {
        Ksuid ksuid = generator.newKsuid();
        assertThat(ksuid).isNotNull();
        assertThat(ksuid.getInstant()).isBefore(now());

        final Instant instant = now();
        ksuid = generator.newKsuid(instant);
        assertThat(ksuid).isNotNull();
        assertThat(ksuid.getInstant()).isEqualTo(instant.truncatedTo(SECONDS));
    }

    @Theory
    public void constructWithSupplierOfIncorrectSize(final int incorrectSize) {
        assertThatCode(() -> {
            new KsuidGenerator(() -> new byte[incorrectSize]);
        }).isExactlyInstanceOf(IllegalArgumentException.class)
          .hasMessage("payloadBytesSupplier must supply byte arrays of length 16");
    }

    @Test
    public void testGenerate() {
        assertThat(KsuidGenerator.generate()).matches("[0-9a-zA-Z]{27}");
    }
}
