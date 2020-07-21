package pl.edu.agh.sukiennik.thesis.operators.combining.combineLatest;

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
public class ReactorCombineLatest {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<String> singleCombineLatestFlux;
    private Flux<String> multiCombineLatestFlux;
    private Flux<String> multiCombineLatestEachOnIoFlux;
    private Flux<String> combineLatestFlux;

    @Setup
    public void setup() {
        singleCombineLatestFlux = Flux.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        multiCombineLatestFlux = Flux.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        multiCombineLatestEachOnIoFlux = Flux.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        combineLatestFlux = Flux.fromArray(IntStream.rangeClosed(times, times * 3 / 2).mapToObj(String::valueOf).toArray(String[]::new));
    }

    @TearDown(Level.Iteration)
    public void clear() {
        ForcedGcMemoryProfiler.recordUsedMemory();
        Schedulers.shutdownNow();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleCombineLatest() {
        Flux.combineLatest(singleCombineLatestFlux, combineLatestFlux, String::concat)
                .then().block();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiCombineLatest() {
        Flux<String> range = multiCombineLatestFlux;
        for (int i = 0; i < 10; i++) {
            range = Flux.combineLatest(range, combineLatestFlux, String::concat);
        }
        range.then().block();
    }

//    @Benchmark
//    @Measurement(iterations = 5, time = 20)
//    public void multiCombineLatestEachOnIo() {
//        Flux<String> range = multiCombineLatestEachOnIoFlux;
//        for (int i = 0; i < 10; i++) {
//            range = Flux.combineLatest(range, combineLatestFlux, String::concat).publishOn(Schedulers.single());
//        }
//        range.then().block();
//    }


    public static void main(String[] args) {
        //ReactorCombineLatest combineLatestBenchmark = new ReactorCombineLatest();
        //combineLatestBenchmark.setup();
        //combineLatestBenchmark.multiCombineLatestEachOnIo();
    }

}

