package pl.edu.agh.sukiennik.thesis.operators.utility.timeout;

import org.openjdk.jmh.annotations.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class ReactorTimeout {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<Integer> singleTimeout;

    @Setup
    public void setup() {
        singleTimeout = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 1)
    public void singleTimeout() {
        singleTimeout
                .timeout(Duration.ofMillis(25))
                .then()
                .block();
    }

    public static void main(String[] args) {
        //ReactorTimeout timeoutBenchmark = new ReactorTimeout();
        //timeoutBenchmark.setup();
        //timeoutBenchmark.singleTimeout();
    }

}

