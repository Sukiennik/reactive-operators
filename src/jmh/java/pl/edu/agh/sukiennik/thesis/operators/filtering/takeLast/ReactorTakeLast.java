package pl.edu.agh.sukiennik.thesis.operators.filtering.takeLast;

import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.operators.ForcedGcMemoryProfiler;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class ReactorTakeLast {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<Integer> singleTakeLast;
    private Flux<Integer> multipleTakeLast;
    private Flux<Integer> multiTakeLastEachOnIo;

    @Setup
    public void setup() {
        singleTakeLast = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multipleTakeLast = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiTakeLastEachOnIo = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleTakeLast() {
        singleTakeLast
                .takeLast(times / 2)
                .then()
                .block();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiTakeLast() {
        Flux<Integer> range = multipleTakeLast;
        int elements = times;
        for (int i = 0; i < 10; i++) {
            elements = elements / 2;
            int takeLastCount = elements;
            range = range.takeLast(takeLastCount);
        }
        range.then().block();
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiTakeLastEachOnIo() {
        Flux<Integer> range = multiTakeLastEachOnIo;
        int elements = times;
        for (int i = 0; i < 10; i++) {
            elements = elements / 2;
            int takeLastCount = elements;
            range = range.publishOn(Schedulers.elastic()).takeLast(takeLastCount);
        }
        range.then().block();
    }

    public static void main(String[] args) {
        //ReactorTakeLast takeLastBenchmark = new ReactorTakeLast();
        //takeLastBenchmark.singleTakeLast();
    }

}

