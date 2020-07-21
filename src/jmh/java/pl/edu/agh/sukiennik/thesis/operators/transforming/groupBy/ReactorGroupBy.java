package pl.edu.agh.sukiennik.thesis.operators.transforming.groupBy;

import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.utils.ForcedGcMemoryProfiler;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class ReactorGroupBy {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<Integer> singleGroupByFlux;
    private Flux<Integer> singleGroupByThenFlattenIndexedFlux;
    private Flux<Integer> singleGroupByEachOnIoFlux;

    @Setup
    public void setup() {
        singleGroupByFlux = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        singleGroupByThenFlattenIndexedFlux = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        singleGroupByEachOnIoFlux = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleGroupBy() {
        singleGroupByFlux
                .groupBy(integer -> integer % 5)
                .then()
                .block();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleGroupByThenFlattenIndexed() {
        singleGroupByThenFlattenIndexedFlux
                .groupBy(integer -> integer % 5)
                .flatMap(Flux::index)
                .then()
                .block();
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleGroupByOnIo() {
        singleGroupByEachOnIoFlux
                .publishOn(Schedulers.elastic())
                .groupBy(integer -> integer % 5)
                .then()
                .block();
    }

    public static void main(String[] args) {
        //ReactorGroupBy flatGroupByBenchmark = new ReactorGroupBy();
        //flatGroupByBenchmark.setup();
        //flatGroupByBenchmark.singleGroupBy();
    }

}



