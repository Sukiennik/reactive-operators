package pl.edu.agh.sukiennik.thesis.operators.utility.initialDelay;

import org.openjdk.jmh.annotations.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class ReactorInitialDelay {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<Integer> singleInitialDelay;

    @Setup
    public void setup() {
        singleInitialDelay = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleInitialDelay() {
        Mono.delay(Duration.ofMillis(25)).thenMany(singleInitialDelay)
                .then()
                .block();
    }

    public static void main(String[] args) {
        //ReactorInitialDelay initialDelayBenchmark = new ReactorInitialDelay();
        //initialDelayBenchmark.setup();
        //initialDelayBenchmark.singleInitialDelay();
    }

}

