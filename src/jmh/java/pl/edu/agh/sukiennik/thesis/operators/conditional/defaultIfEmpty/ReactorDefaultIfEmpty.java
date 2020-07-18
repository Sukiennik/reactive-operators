package pl.edu.agh.sukiennik.thesis.operators.conditional.defaultIfEmpty;

import org.openjdk.jmh.annotations.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class ReactorDefaultIfEmpty {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<Integer> singleDefaultIfEmpty;

    @Setup
    public void setup() {
        singleDefaultIfEmpty = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleDefaultIfEmpty() {
        singleDefaultIfEmpty
                .defaultIfEmpty(1)
                .then()
                .block();
    }

    public static void main(String[] args) {
        //ReactorDefaultIfEmpty defaultIfEmptyBenchmark = new ReactorDefaultIfEmpty();
        //defaultIfEmptyBenchmark.singleDefaultIfEmpty();
    }

}

