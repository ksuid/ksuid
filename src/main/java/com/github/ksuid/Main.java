package com.github.ksuid;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Main program for generating ksuids from command-line.
 *
 * <p>
 * Mimics https://github.com/segmentio/ksuid/blob/v1.0.4/cmd/ksuid/main.go
 */
public final class Main {

    private final PrintStream printStream;
    private final IntConsumer exit;
    private final Random random;
    private final Clock clock;
    private final Flags flags = new Flags();
    private final Map<String, Consumer<Ksuid>> printers;

    public static void main(final String... args) {
        final Main main = new Main(System.out, System::exit, new SecureRandom(), Clock.systemUTC());
        main.run(args);
    }

    Main(final PrintStream printStream, final IntConsumer exit, final Random random, final Clock clock) {
        this.printStream = printStream;
        this.exit = exit;
        this.random = random;
        this.clock = clock;

        printers = new HashMap<>();
        printers.put("string", this::printString);
        printers.put("inspect", this::printInspect);
        printers.put("time", this::printTime);
        printers.put("timestamp", this::printTimestamp);
        printers.put("payload", this::printPayload);
        printers.put("raw", this::printRaw);
        printers.put("template", this::printTemplate);
    }

    public void run(final String... args) {
        try {
            tryRun(args);
        } catch (final CliException e) {
            printStream.println(e.getMessage());
            printUsage(1);
        }
    }

    private void tryRun(final String... args) {
        parseFlags(args);

        final Consumer<Ksuid> printer = printers.get(flags.format);

        final List<Ksuid> ksuids = new ArrayList<>();
        final KsuidGenerator ksuidGenerator = new KsuidGenerator(random);
        if (flags.positionalArguments.isEmpty()) {
            IntStream.range(0, flags.count)
                    .forEach(any -> {
                        final Instant now = Instant.now(clock);
                        final Ksuid ksuid = ksuidGenerator.newKsuid(now);
                        ksuids.add(ksuid);
                    });
        }

        flags.positionalArguments.forEach(arg -> ksuids.add(parse(arg)));

        ksuids.forEach(ksuid -> {
            if (flags.verbose) {
                printStream.printf("%s: ", ksuid);
            }
            printer.accept(ksuid);
        });
    }

    private Ksuid parse(final String arg) {
        try {
            return Ksuid.fromString(arg);
        } catch (final IllegalArgumentException e) {
            throw new CliException("Error when parsing \"" + arg + "\": Valid encoded KSUIDs are 27 characters");
        }
    }

    private void parseFlags(final String... args) {
        boolean positionArgsOnly = false;
        final Iterator<String> iterator = Arrays.asList(args).iterator();
        while (iterator.hasNext()) {
            final String flag = iterator.next();

            if (!flag.startsWith("-")) {
                positionArgsOnly = true;
            }

            if (positionArgsOnly) {
                flags.positionalArguments.add(flag);
                continue;
            }

            final Supplier<String> value = () -> {
                if (!iterator.hasNext()) {
                    throw new CliException("flag needs an argument: " + flag);
                }
                return iterator.next();
            };

            switch (flag) {
                case "-n":
                    final String countValue = value.get();
                    try {
                        flags.count = Integer.parseInt(countValue);
                    } catch (final Exception e) {
                        throw new CliException("invalid value \"" + countValue + "\" for flag -n: parse error");
                    }
                    break;

                case "-f":
                    final String formatValue = value.get();
                    if (!printers.containsKey(formatValue)) {
                        throw new CliException("Bad formatting function: " + formatValue);
                    }
                    flags.format = formatValue;
                    break;

                case "-t":
                    flags.templateText = value.get();
                    break;

                case "-v":
                    flags.verbose = true;
                    break;

                case "-h":
                    printUsage(0);
                    break;

                default:
                    throw new CliException("flag provided but not defined: " + flag);
            }
        }
    }

    private void printUsage(final int exitCode) {
        printStream.print("Usage of ksuid:\n"
                + "  -f string\n"
                + "        One of string, inspect, time, timestamp, payload, raw, or template. (default \"string\")\n"
                + "  -n int\n"
                + "        Number of KSUIDs to generate when called with no other arguments. (default 1)\n"
                + "  -t string\n"
                + "        The Go template used to format the output.\n"
                + "  -v    Turn on verbose mode.\n"
                + "");
        exit.accept(exitCode);
    }

    private void printString(final Ksuid ksuid) {
        printStream.println(ksuid);
    }

    private void printInspect(final Ksuid ksuid) {
        printStream.println(ksuid.toInspectString());
    }

    private void printTime(final Ksuid ksuid) {
        printStream.println(ksuid.getTime());
    }

    private void printTimestamp(final Ksuid ksuid) {
        printStream.println(ksuid.getTimestamp());
    }

    private void printPayload(final Ksuid ksuid) {
        printByteArray(ksuid.getPayload());
    }

    private void printRaw(final Ksuid ksuid) {
        printByteArray(ksuid.asRaw());
    }

    private void printTemplate(final Ksuid ksuid) {
        String result = flags.templateText;
        result = result.replace("{{.String}}", ksuid.toString());
        result = result.replace("{{.Raw}}", ksuid.asRaw());
        result = result.replace("{{.Time}}", ksuid.getTime());
        result = result.replace("{{.Timestamp}}", ksuid.getTimestamp() + "");
        result = result.replace("{{.Payload}}", ksuid.getPayload());
        printStream.println(result);
    }

    private void printByteArray(final String hexBytes) {
        try {
            printStream.write(Hex.hexDecode(hexBytes));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static class Flags {
        private int count = 1;
        private String format = "string";
        private String templateText = "";
        private boolean verbose;
        private final List<String> positionalArguments = new ArrayList<>();
    }

    private static class CliException extends RuntimeException {

        private static final long serialVersionUID = -7135545958349833195L;

        public CliException(final String msg) {
            super(msg);
        }
    }
}
