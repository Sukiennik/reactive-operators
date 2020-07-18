package pl.edu.agh.sukiennik.thesis.operators.filtering.last;

import org.openjdk.jmh.annotations.*;
import reactor.core.publisher.Flux;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class ReactorLast {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<Integer> singleLast;

    @Setup
    public void setup() {
        singleLast = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleLast() {
        singleLast
                .last()
                .block();
    }

    public static void main(String[] args) {
        //ReactorLast lastBenchmark = new ReactorLast();
        //lastBenchmark.singleLast();
    }

}

