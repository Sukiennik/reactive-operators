package pl.edu.agh.sukiennik.thesis.operators.transforming.window;

import org.openjdk.jmh.annotations.*;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class ReactorWindow {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<Integer> singleWindowFlux;
    private Flux<Integer> singleWindowThenFlattenIndexedFlux;
    private Flux<Integer> multiWindowFlux;
    private Flux<Integer> multiWindowEachOnIoFlux;

    @Setup
    public void setup() {
        singleWindowFlux = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        singleWindowThenFlattenIndexedFlux = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiWindowFlux = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiWindowEachOnIoFlux = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleWindow() {
        singleWindowFlux
                .window(5)
                .then()
                .block();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleWindowThenFlattenIndexed() {
        singleWindowThenFlattenIndexedFlux
                .window(5)
                .flatMap(Flux::index)
                .then()
                .block();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiWindow() {
        multiWindowFlux
                .window(5)
                .window(5)
                .window(5)
                .window(5)
                .window(5)
                .window(5)
                .window(5)
                .window(5)
                .window(5)
                .window(5)
                .then().block();
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiWindowEachOnIo() {
        multiWindowEachOnIoFlux
                .publishOn(Schedulers.elastic()).window(5)
                .publishOn(Schedulers.elastic()).window(5)
                .publishOn(Schedulers.elastic()).window(5)
                .publishOn(Schedulers.elastic()).window(5)
                .publishOn(Schedulers.elastic()).window(5)
                .publishOn(Schedulers.elastic()).window(5)
                .publishOn(Schedulers.elastic()).window(5)
                .publishOn(Schedulers.elastic()).window(5)
                .publishOn(Schedulers.elastic()).window(5)
                .publishOn(Schedulers.elastic()).window(5)
                .then().block();
    }


    public static void main(String[] args) {
        //ReactorWindow windowBenchmark = new ReactorWindow();
        //windowBenchmark.setup();
        //windowBenchmark.singleWindowThenFlattenIndexed();
    }

}



