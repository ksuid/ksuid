package com.github.ksuid;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    public void newKsuidAtTimestampRangeBoundaries(final KsuidGenerator generator) {
        final Instant min = Instant.parse("2014-05-13T16:53:20Z");
        final Instant max = Instant.parse("2150-06-19T23:21:35Z");

        assertThat(generator.newKsuid(min).getInstant()).isEqualTo(min);
        assertThat(generator.newKsuid(max).getInstant()).isEqualTo(max);
    }

    @Theory
    public void newKsuidRejectsInstantsOutsideTimestampRange(final KsuidGenerator generator) {
        for (final Instant instant : new Instant[] {
                Instant.parse("2014-05-13T16:53:19Z"),
                Instant.parse("2000-01-01T00:00:00Z"),
                Instant.parse("2150-06-19T23:21:36Z")}) {
            assertThatThrownBy(() -> generator.newKsuid(instant))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("instant must be between 2014-05-13T16:53:20Z and 2150-06-19T23:21:35Z (inclusive): " + instant);
        }
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
