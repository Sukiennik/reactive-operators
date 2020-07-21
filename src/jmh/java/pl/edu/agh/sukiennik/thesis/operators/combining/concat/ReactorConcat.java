package pl.edu.agh.sukiennik.thesis.operators.combining.concat;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
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
public class ReactorConcat {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<String> singleConcatFlux;
    private Flux<String> multiConcatFlux;
    private Flux<String> multiConcatEachOnIoFlux;
    private Flux<String> concatFlux;

    @Setup
    public void setup() {
        singleConcatFlux = Flux.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        multiConcatFlux = Flux.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        multiConcatEachOnIoFlux = Flux.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        concatFlux = Flux.fromArray(IntStream.rangeClosed(times, times * 3 / 2).mapToObj(String::valueOf).toArray(String[]::new));
    }

    @TearDown(Level.Iteration)
    public void clear() {
        ForcedGcMemoryProfiler.recordUsedMemory();
        Schedulers.shutdownNow();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleConcat() {
        singleConcatFlux
                .concatWith(concatFlux)
                .then().block();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiConcat() {
        Flux<String> range = multiConcatFlux;
        for (int i = 0; i < 10; i++) {
            range = range.concatWith(concatFlux);
        }
        range.then().block();
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiConcatEachOnIo(Blackhole bh) {
        Flux<String> range = multiConcatEachOnIoFlux;
        for (int i = 0; i < 10; i++) {
            range = range.publishOn(Schedulers.elastic()).concatWith(concatFlux);
        }
        range.then().block();
    }


    public static void main(String[] args) {
        //ReactorConcat concatBenchmark = new ReactorConcat();
        //concatBenchmark.singleConcat();
    }

}

