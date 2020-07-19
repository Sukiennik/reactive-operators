package pl.edu.agh.sukiennik.thesis.operators.filtering.filter;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
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
public class ReactorFilter {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<Integer> singleFilterFlux;
    private Flux<Integer> multiFilterFlux;
    private Flux<Integer> multiFilterEachOnIoFlux;

    @Setup
    public void setup() {
        singleFilterFlux = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiFilterFlux = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiFilterEachOnIoFlux = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleFilter() {
        singleFilterFlux
                .filter(element -> element < times / 2)
                .then().block();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiFilter() {
        Flux<Integer> range = multiFilterFlux;
        int condition = times;
        for (int i = 0; i < 10; i++) {
            condition = condition / 2;
            int finalCondition = condition;
            range = range.filter(element -> element < finalCondition);
        }
        range.then().block();
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiFilterEachOnIo(Blackhole bh) {
        Flux<Integer> range = multiFilterEachOnIoFlux;
        int condition = times;
        for (int i = 0; i < 10; i++) {
            condition = condition / 2;
            int finalCondition = condition;
            range = range.publishOn(Schedulers.elastic()).filter(element -> element < finalCondition);
        }
        range.then().block();
    }


    public static void main(String[] args) {
        //ReactorFilter filterBenchmark = new ReactorFilter();
        //filterBenchmark.singleFilter();
    }

}

