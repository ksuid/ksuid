package com.github.ksuid;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@RunWith(Theories.class)
public class KsuidTest {
    private static final String PAYLOAD_RAW = "B5A1CD34B5F99D1154FB6853345C9735";
    private static final byte[] PAYLOAD_BYTES = Hex.hexDecode(PAYLOAD_RAW);
    private static final String KSUID_RAW = "0669F7EF" + PAYLOAD_RAW;
    private static final byte[] KSUID_BYTES = Hex.hexDecode(KSUID_RAW);
    private static final String KSUID_STRING = "0ujtsYcgvSTl8PAuAdqWYSMnLOv";
    private static final int TIMESTAMP = 107608047;
    private static final int EPOCH = 1400000000;
    private static final Instant INSTANT = Instant.ofEpochSecond((long) TIMESTAMP + EPOCH);
    private static final String TIME = String.format("%1$tF %1$tT %1$tz %1$tZ", INSTANT.atZone(ZoneId.systemDefault()));
    private static final String TIME_UTC = String.format("%1$tF %1$tT %1$tz %1$tZ", INSTANT.atZone(ZoneId.of("UTC")));


    @DataPoints
    public static final int[] INCORRECT_SIZES = {0, 10, 25}; // anything but 16

    @DataPoints
    public static final Ksuid[] KSUIDS = {
            Ksuid.newBuilder().withKsuidBytes(KSUID_BYTES).build(),
            Ksuid.newBuilder().withKsuidString(KSUID_STRING).build(),
            Ksuid.newBuilder().withTimestamp(TIMESTAMP).withPayload(PAYLOAD_BYTES).build()
    };

    @Theory
    public void asBytes(final Ksuid ksuid) {
        assertThat(ksuid.asBytes()).isEqualTo(KSUID_BYTES);
    }

    @Theory
    public void asString(final Ksuid ksuid) {
        assertThat(ksuid.asString()).isEqualTo(KSUID_STRING);
    }

    @Theory
    public void asRaw(final Ksuid ksuid) {
        assertThat(ksuid.asRaw()).isEqualTo(KSUID_RAW);
    }

    @Theory
    public void getInstant(final Ksuid ksuid) {
        assertThat(ksuid.getInstant()).isEqualTo(Instant.ofEpochSecond(TIMESTAMP + EPOCH));
    }

    @Theory
    public void getTime(final Ksuid ksuid) {
        assertThat(ksuid.getTime()).isEqualTo(TIME);
    }

    @Theory
    public void getTimeInZone(final Ksuid ksuid) {
        assertThat(ksuid.getTime(ZoneId.of("UTC"))).isEqualTo(TIME_UTC);
    }

    @Theory
    public void getTimestamp(final Ksuid ksuid) {
        assertThat(ksuid.getTimestamp()).isEqualTo(TIMESTAMP);
    }

    @Theory
    public void getPayload(final Ksuid ksuid) {
        assertThat(ksuid.getPayload()).isEqualTo(PAYLOAD_RAW);
    }

    @Theory
    public void toInspectString(final Ksuid ksuid) {
        final String s = String.format("REPRESENTATION:%n%n  String: %1$s%n     Raw: %2$s%n%nCOMPONENTS:%n%n       Time: %3$s%n  Timestamp: %4$d%n    Payload: %5$s%n",
                                       KSUID_STRING, KSUID_RAW, TIME, TIMESTAMP, PAYLOAD_RAW);
        assertThat(ksuid.toInspectString()).isEqualTo(s);
    }

    @Theory
    public void testToString(final Ksuid ksuid) {
        assertThat(ksuid.toString()).isEqualTo("Ksuid[timestamp = " + TIMESTAMP +
                                                       ", payload = [-75, -95, -51, 52, -75, -7, -99, 17, 84, -5, 104, 83, 52, 92, -105, 53]" +
                                                       ", ksuidBytes = [6, 105, -9, -17, -75, -95, -51, 52, -75, -7, -99, 17, 84, -5, 104, 83, 52, 92, -105, 53]]");
    }

    @Test
    public void equalsAndHashcode() {
        EqualsVerifier.forClass(Ksuid.class).verify();
        assertThat(KSUIDS[0]).isEqualTo(KSUIDS[1]);
        assertThat(KSUIDS[1]).isEqualTo(KSUIDS[2]);
    }

    @Test
    public void comparableIsConsistentWithEquals() {
        assertThat(KSUIDS[0].compareTo(KSUIDS[1])).isEqualTo(0);
        assertThat(KSUIDS[1].compareTo(KSUIDS[2])).isEqualTo(0);
    }

    @Test
    public void comparable() {
        final Clock utc = Clock.systemUTC();

        final KsuidGenerator generator = new KsuidGenerator(new SecureRandom());
        final Ksuid first = generator.newKsuid(Instant.now(Clock.offset(utc, Duration.ofSeconds(2))));
        final Ksuid second = generator.newKsuid(Instant.now(Clock.offset(utc, Duration.ofSeconds(4))));
        final Ksuid third = generator.newKsuid(Instant.now(Clock.offset(utc, Duration.ofSeconds(6))));
        final Ksuid fourth = generator.newKsuid(Instant.now(Clock.offset(utc, Duration.ofSeconds(8))));

        final List<Ksuid> orderedList = Arrays.asList(first, second, third, fourth);
        final List<Ksuid> list = Arrays.asList(first, second, third, fourth);
        while (list.equals(orderedList)) {
            Collections.shuffle(list);
        }
        Collections.sort(list);
        assertThat(list).isEqualTo(orderedList);
    }

    @Theory
    public void constructWithIncorrectPayloadSize(final int incorrectSize) {
        assertThatCode(() -> Ksuid.newBuilder().withTimestamp(TIMESTAMP).withPayload(new byte[incorrectSize]).build())
                .hasStackTraceContaining("payload is not expected length of 16 bytes");
    }

    @Theory
    public void constructWithIncorrectKsuidBytesSize(final int incorrectSize) {
        assertThatCode(() -> Ksuid.newBuilder().withKsuidBytes(new byte[incorrectSize]).build())
                .hasStackTraceContaining("ksuid is not expected length of 20 bytes");
    }

    @Theory
    public void constructWithIncorrectKsuidStringSize(final int incorrectSize) {
        assertThatCode(() -> {
            Ksuid.newBuilder()
                 .withKsuidString(IntStream.range(0, incorrectSize).mapToObj(i -> "a").collect(joining()))
                 .build();
        }).hasStackTraceContaining("ksuid is not expected length of 20 bytes");
    }
}
