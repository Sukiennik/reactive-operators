package pl.edu.agh.sukiennik.thesis.operators.combining.zip;

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
public class ReactorZip {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<String> singleZipFlux;
    private Flux<String> multiZipFlux;
    private Flux<String> multiZipEachOnIoFlux;
    private Flux<String> zipFlux;

    @Setup
    public void setup() {
        singleZipFlux = Flux.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        multiZipFlux = Flux.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        multiZipEachOnIoFlux = Flux.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        zipFlux = Flux.fromArray(IntStream.rangeClosed(times, times * 3 / 2).mapToObj(String::valueOf).toArray(String[]::new));
    }

    @TearDown(Level.Iteration)
    public void clear() {
        ForcedGcMemoryProfiler.recordUsedMemory();
        Schedulers.shutdownNow();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleZip() {
        singleZipFlux
                .zipWith(zipFlux, String::concat)
                .then().block();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiZip() {
        Flux<String> range = multiZipFlux;
        for (int i = 0; i < 10; i++) {
            range = range.zipWith(zipFlux, String::concat);
        }
        range.then().block();
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiZipEachOnIo(Blackhole bh) {
        Flux<String> range = multiZipEachOnIoFlux;
        for (int i = 0; i < 10; i++) {
            range = range.publishOn(Schedulers.elastic()).zipWith(zipFlux, String::concat);
        }
        range.then().block();
    }


    public static void main(String[] args) {
        //ReactorZip zipBenchmark = new ReactorZip();
        //zipBenchmark.singleZip();
    }

}

