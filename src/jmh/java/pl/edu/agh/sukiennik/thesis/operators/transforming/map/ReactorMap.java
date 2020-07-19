package pl.edu.agh.sukiennik.thesis.operators.transforming.map;

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
public class ReactorMap {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<Integer> singleMapFlux;
    private Flux<Integer> multiMapFlux;
    private Flux<Integer> multiMapEachOnIoFlux;

    @Setup
    public void setup() {
        singleMapFlux = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiMapFlux = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiMapEachOnIoFlux = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @TearDown(Level.Iteration)
    public void cleanup() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }
    
    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleMap() {
        singleMapFlux
                .map(element -> element + 1)
                .then()
                .block();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiMap() {
        Flux<Integer> range = multiMapFlux;
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            range = range.map(element -> element + finalI);
        }
        range.then().block();
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiMapEachOnIo() {
        Flux<Integer> range = multiMapEachOnIoFlux;
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            range = range.publishOn(Schedulers.elastic()).map(element -> element + finalI);
        }
        range.then().block();
    }


    public static void main(String[] args) {
        //ReactorMap mapBenchmark = new ReactorMap();
        //mapBenchmark.multiMapEachOnIo();
    }

}



