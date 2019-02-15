package com.xoom.inf.ksuid;

import java.time.Instant;
import java.util.Random;
import java.util.function.Supplier;

import static com.xoom.inf.ksuid.Ksuid.EPOCH;
import static com.xoom.inf.ksuid.Ksuid.PAYLOAD_BYTES;

/**
 * Generate K-Sortable Globally Unique IDs (KSUID).
 * <p>
 * Unless otherwise noted, passing a {@code null} argument to a method of this class
 * will cause a {@link java.lang.NullPointerException NullPointerException} to be thrown.
 * <p>
 * See <a href="https://github.com/segmentio/ksuid">https://github.com/segmentio/ksuid</a>.
 */
@SuppressWarnings("WeakerAccess")
public class KsuidGenerator {
    private final Supplier<byte[]> payloadSupplier;

    /**
     * Construct a KSUID generator.
     *
     * @param random source of random bytes for payload, SecureRandom is recommended
     */
    public KsuidGenerator(final Random random) {
        this(() -> {
            final byte[] payload = new byte[PAYLOAD_BYTES];
            random.nextBytes(payload);
            return payload;
        });
    }

    /**
     * Construct a KSUID generator.
     *
     * @param payloadSupplier supplier of byte arrays which must be {@link Ksuid#PAYLOAD_BYTES PAYLOAD_BYTES} in length
     */
    public KsuidGenerator(final Supplier<byte[]> payloadSupplier) {
        if (payloadSupplier.get().length != PAYLOAD_BYTES) {
            throw new IllegalArgumentException("payloadBytesSupplier must supply byte arrays of length " + PAYLOAD_BYTES);
        }
        this.payloadSupplier = payloadSupplier;
    }

    /**
     * Generate a new KSUID.
     * <p>
     * The equivalent of calling {@link #newKsuid(Instant) newKsuid(Instant.now())}.
     *
     * @return a Ksuid object
     */
    public Ksuid newKsuid() {
        return newKsuid(Instant.now());
    }

    /**
     * Generate a new KSUID with a timestamp component derived from an Instant.
     *
     * @param instant an Instant from which to derive the timestamp component
     * @return a Ksuid object
     */
    public Ksuid newKsuid(final Instant instant) {
        return Ksuid.newBuilder()
                    .withTimestamp((int) (instant.toEpochMilli() / 1000 - EPOCH)) // 4 bytes
                    .withPayload(payloadSupplier.get()) // 16 bytes
                    .build();
    }

}
