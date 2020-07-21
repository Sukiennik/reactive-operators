package pl.edu.agh.sukiennik.thesis.operators.conditional.equal;

import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.utils.ForcedGcMemoryProfiler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class ReactorEquals {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<Integer> singleEqual;
    private Flux<Integer> singleEqual2;

    @Setup
    public void setup() {
        singleEqual = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        singleEqual2 = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleEqual() {
        Mono.sequenceEqual(singleEqual, singleEqual2)
                .then()
                .block();
    }

    public static void main(String[] args) {
        //ReactorEqual equalBenchmark = new ReactorEqual();
        //equalBenchmark.singleEqual();
    }

}

