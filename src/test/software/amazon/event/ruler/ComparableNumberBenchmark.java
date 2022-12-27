package software.amazon.event.ruler;

import com.fasterxml.jackson.core.io.doubleparser.FastDoubleParser;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.Random;

@State(Scope.Benchmark)
public class ComparableNumberBenchmark {

    public static void main(String[] args) throws Exception {

        Options opts = new OptionsBuilder()
                .warmupIterations(5)
                .warmupTime(TimeValue.seconds(2))
                .measurementTime(TimeValue.seconds(5))
                .forks(1)
                .build();

        new Runner(opts).run();

    }

    static final int DATA_SET_SAMPLE_SIZE = 1024;

    String[] random_values;

    @Setup
    public void setup() {

        Random random = new Random();
        random_values = new String[DATA_SET_SAMPLE_SIZE];
        for (int i = 0; i < DATA_SET_SAMPLE_SIZE; i++) {
            random_values[i] = Double.toString(random.nextDouble());
            if (!generateOriginal(random_values[i]).equals(generateNew(random_values[i]))) {
                throw new RuntimeException("not equals");
            }
        }

    }

    @Benchmark
    public void original(final Blackhole bh) {

        for (String v : random_values) {
            bh.consume(generateOriginal(v));
        }

    }

    @Benchmark
    public void candidate(final Blackhole bh) {

        for (String v : random_values) {
            bh.consume(generateNew(v));
        }

    }

    public static String generateNew(String number) {
        double f = FastDoubleParser.parseDouble(number);
        return ComparableNumber.generate(f);
    }

    public static String generateOriginal(String number) {
        double f = Double.parseDouble(number);
        return ComparableNumber.generate(f);
    }

}