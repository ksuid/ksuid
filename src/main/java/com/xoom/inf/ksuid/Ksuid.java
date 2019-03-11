package com.xoom.inf.ksuid;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.StringJoiner;

import static com.xoom.inf.ksuid.Base62.base62Decode;
import static com.xoom.inf.ksuid.Base62.base62Encode;
import static com.xoom.inf.ksuid.Hex.hexEncode;

/**
 * A K-Sortable Globally Unique ID (KSUID).
 * <p>
 * Using {@link KsuidGenerator} is the recommended way to create Ksuid objects.
 * <p>
 * See <a href="https://github.com/segmentio/ksuid">https://github.com/segmentio/ksuid</a>.
 */
@SuppressWarnings("WeakerAccess")
public class Ksuid implements Comparable<Ksuid> {
    static final int EPOCH = 1400000000;
    public static final int PAYLOAD_BYTES = 16;

    private static final int TIMESTAMP_BYTES = 4;
    private static final int TOTAL_BYTES = TIMESTAMP_BYTES + PAYLOAD_BYTES;
    private static final int PAD_TO_LENGTH = 27;
    private static final Comparator<Ksuid> COMPARATOR = Comparator.comparingInt(Ksuid::getTimestamp)
                                                                  .thenComparing(Ksuid::getPayload);

    private final int timestamp;
    private final byte[] payload;
    private final byte[] ksuidBytes;

    private Ksuid(final Builder builder) {
        if (builder.ksuidBytes != null) {
            if (builder.ksuidBytes.length != TOTAL_BYTES) {
                throw new IllegalArgumentException("ksuid is not expected length of " + TOTAL_BYTES + " bytes");
            }

            ksuidBytes = builder.ksuidBytes;
            final ByteBuffer byteBuffer = ByteBuffer.wrap(ksuidBytes);
            timestamp = byteBuffer.getInt();
            payload = new byte[PAYLOAD_BYTES];
            byteBuffer.get(payload);
        } else {
            if (builder.payload.length != PAYLOAD_BYTES) {
                throw new IllegalArgumentException("payload is not expected length of " + PAYLOAD_BYTES + " bytes");
            }

            timestamp = builder.timestamp;
            payload = builder.payload;
            ksuidBytes = ByteBuffer.allocate(TOTAL_BYTES)
                                   .putInt(timestamp)
                                   .put(payload)
                                   .array();
        }
    }

    /**
     * A builder to create a {@link Ksuid}.
     *
     * @return builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Get the KSUID as a byte array.
     *
     * @return KSUID bytes
     */
    public byte[] asBytes() {
        return Arrays.copyOf(ksuidBytes, ksuidBytes.length);
    }

    /**
     * Get the KSUID as a string.
     *
     * @return KSUID string
     */
    public String asString() {
        return base62Encode(ksuidBytes, PAD_TO_LENGTH);
    }

    /**
     * Get the KSUID as a hex string.
     *
     * @return KSUID hex string
     */
    public String asRaw() {
        return hexEncode(ksuidBytes);
    }

    /**
     * Get the KSUID time component as an Instant.
     *
     * @return an Instant
     */
    public Instant getInstant() {
        return Instant.ofEpochSecond((long) timestamp + EPOCH);
    }

    /**
     * Get the KSUID time component in the system default timezone.
     *
     * @return KSUID time component string
     */
    public String getTime() {
        return getTime(ZoneId.systemDefault());
    }

    /**
     * Get the KSUID time component in the provided timezone.
     *
     * @param zoneId the timezone
     * @return KSUID time component string
     */
    public String getTime(final ZoneId zoneId) {
        return String.format("%1$tF %1$tT %1$tz %1$tZ", getInstant().atZone(zoneId));
    }

    /**
     * Get the KSUID timestamp component.
     *
     * @return KSUID timestamp component
     */
    public int getTimestamp() {
        return timestamp;
    }

    /**
     * Get the KSUID payload component.
     *
     * @return KSUID payload component
     */
    public String getPayload() {
        return hexEncode(payload);
    }

    /**
     * Get the KSUID inspect formatting string representation.
     *
     * @return KSUID inspect formatting string
     */
    public String toInspectString() {
        return String.format("REPRESENTATION:%n%n  String: %1$s%n     Raw: %2$s%n%nCOMPONENTS:%n%n       Time: %3$s%n  Timestamp: %4$d%n    Payload: %5$s%n",
                             asString(), asRaw(), getTime(), getTimestamp(), getPayload());
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Ksuid)) {
            return false;
        }

        final Ksuid that = (Ksuid) o;

        return Objects.equals(this.timestamp, that.timestamp) &&
                Arrays.equals(this.payload, that.payload) &&
                Arrays.equals(this.ksuidBytes, that.ksuidBytes);
    }

    @Override
    public final int hashCode() {
        int result = Objects.hash(timestamp);
        result = 31 * result + Arrays.hashCode(payload);
        result = 31 * result + Arrays.hashCode(ksuidBytes);
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", this.getClass().getSimpleName() + "[", "]")
                .add("timestamp = " + timestamp)
                .add("payload = " + Arrays.toString(payload))
                .add("ksuidBytes = " + Arrays.toString(ksuidBytes))
                .toString();
    }

    @Override
    public int compareTo(@SuppressWarnings("NullableProblems") final Ksuid other) {
        return COMPARATOR.compare(this, other);
    }


    /**
     * Builder to create a {@link Ksuid}.
     * <p>
     * Get a Builder using {@link Ksuid#newBuilder() Ksuid.newBuilder()}.
     */
    public static final class Builder {
        private int timestamp;
        private byte[] payload;
        private byte[] ksuidBytes;

        private Builder() {
        }

        /**
         * Specify the timestamp component of a KSUID.
         * <p>
         * The payload must also be specified before {@link #build()} is called.
         *
         * @param timestamp the timestamp component
         * @return this builder
         */
        public Builder withTimestamp(final int timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /**
         * Specify the payload component of a KSUID.
         * <p>
         * The timestamp must also be specified before {@link #build()} is called.
         *
         * @param payload the payload component
         * @return this builder
         */
        public Builder withPayload(final byte[] payload) {
            this.payload = payload;
            return this;
        }

        /**
         * Specify the KSUID bytes of the form returned by {@link #asBytes()}.
         * <p>
         * The values for payload and timestamp will be ignored when {@link #build()} is called.
         *
         * @param ksuidBytes the KSUID bytes
         * @return this builder
         */
        public Builder withKsuidBytes(final byte[] ksuidBytes) {
            this.ksuidBytes = ksuidBytes;
            return this;
        }

        /**
         * Specify the KSUID string of the form returned by {@link #asString()}.
         * <p>
         * The values for payload and timestamp will be ignored when {@link #build()} is called.
         *
         * @param ksuidString the KSUID string
         * @return this builder
         */
        public Builder withKsuidString(final String ksuidString) {
            this.ksuidBytes = base62Decode(ksuidString);
            return this;
        }

        /**
         * Build a {@link Ksuid} instance.
         * <p>
         * You must have specified either KSUID bytes, KSUID string, or a timestamp and payload.
         *
         * @return a new {@link Ksuid} instance
         */
        public Ksuid build() {
            return new Ksuid(this);
        }
    }

}
