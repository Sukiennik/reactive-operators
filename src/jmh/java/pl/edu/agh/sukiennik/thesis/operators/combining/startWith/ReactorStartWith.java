package pl.edu.agh.sukiennik.thesis.operators.combining.startWith;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class ReactorStartWith {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<String> singleStartWithFlux;
    private Flux<String> multiStartWithFlux;
    private Flux<String> multiStartWithEachOnIoFlux;
    private Flux<String> startWithFlux;

    @Setup
    public void setup() {
        singleStartWithFlux = Flux.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        multiStartWithFlux = Flux.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        multiStartWithEachOnIoFlux = Flux.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        startWithFlux = Flux.fromArray(IntStream.rangeClosed(times, times * 3 / 2).mapToObj(String::valueOf).toArray(String[]::new));
    }

    @TearDown(Level.Iteration)
    public void clear() {
        Schedulers.shutdownNow();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleStartWith() {
        singleStartWithFlux
                .startWith(startWithFlux)
                .then().block();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiStartWith() {
        Flux<String> range = multiStartWithFlux;
        for (int i = 0; i < 10; i++) {
            range = range.startWith(startWithFlux);
        }
        range.then().block();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiStartWithEachOnIo(Blackhole bh) {
        Flux<String> range = multiStartWithEachOnIoFlux;
        for (int i = 0; i < 10; i++) {
            range = range.publishOn(Schedulers.elastic()).startWith(startWithFlux);
        }
        range.then().block();
    }


    public static void main(String[] args) {
        //ReactorStartWith startWithBenchmark = new ReactorStartWith();
        //startWithBenchmark.singleStartWith();
    }

}

