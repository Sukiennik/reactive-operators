package pl.edu.agh.sukiennik.thesis.operators.combining.merge;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class ReactorMerge {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<String> singleMergeFlux;
    private Flux<String> multiMergeFlux;
    private Flux<String> multiMergeEachOnIoFlux;
    private Flux<String> mergedFlux;

    @Setup
    public void setup() {
        singleMergeFlux = Flux.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        multiMergeFlux = Flux.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        multiMergeEachOnIoFlux = Flux.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        mergedFlux = Flux.fromArray(IntStream.rangeClosed(times, times * 3 / 2).mapToObj(String::valueOf).toArray(String[]::new));

    }

    @Benchmark
    @Measurement(iterations = 5, time = 5)
    public void singleMerge() {
        singleMergeFlux
                .mergeWith(mergedFlux)
                .then().block();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 10)
    public void multiMerge() {
        Flux<String> range = multiMergeFlux;
        for (int i = 0; i < 10; i++) {
            range = range.mergeWith(mergedFlux);
        }
        range.then().block();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiMergeEachOnIo(Blackhole bh) {
        Flux<String> range = multiMergeEachOnIoFlux;
        for (int i = 0; i < 10; i++) {
            range = range.publishOn(Schedulers.elastic()).mergeWith(mergedFlux);
        }
        range.then().block();
    }


    public static void main(String[] args) {
        //ReactorMerge mergeBenchmark = new ReactorMerge();
        //mergeBenchmark.singleMerge();
    }

}

