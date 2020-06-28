package pl.edu.agh.sukiennik.thesis.operators.transforming.concatMap;

import org.openjdk.jmh.annotations.*;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class ReactorConcatMap {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<String> characters;
    private Flux<Integer> singleConcatMapFlux;
    private Flux<Integer> multiConcatMapFlux;
    private Flux<Integer> multiConcatMapEachOnIoFlux;

    @Setup
    public void setup() {
        characters = Flux.just("A", "B");
        singleConcatMapFlux = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiConcatMapFlux = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiConcatMapEachOnIoFlux = Flux.fromArray(IntStream.rangeClosed(0, 10000).boxed().toArray(Integer[]::new));
    }
    
    @Benchmark
    @Measurement(iterations = 5, time = 5)
    public void singleConcatMap() {
        singleConcatMapFlux
                .concatMap(integer -> characters.map(character -> character + integer.toString()), 4)
                .then()
                .block();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 10)
    public void multiConcatMap() {
        Flux<String> results =  null;
        for (int i = 0; i < 10; i++) {
            if(results == null) {
                results = multiConcatMapFlux.concatMap(integer -> characters.map(character -> character + integer.toString()), 4);
            } else {
                results = results.concatMap(string -> characters.map(character -> character + string), 4);
            }
        }
        results.then().block();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiConcatMapEachOnIo() {
        Flux<String> results =  null;
        for (int i = 0; i < 10; i++) {
            if(results == null) {
                results = multiConcatMapEachOnIoFlux
                        .publishOn(Schedulers.elastic())
                        .concatMap(integer -> characters.map(character -> character + integer.toString()), 4);
            } else {
                results = results
                        .publishOn(Schedulers.elastic())
                        .concatMap(string -> characters.map(character -> character + string), 4);
            }
        }
        results.then().block();
    }


    public static void main(String[] args) {
        ReactorConcatMap flatConcatMapBenchmark = new ReactorConcatMap();
        flatConcatMapBenchmark.setup();
        flatConcatMapBenchmark.multiConcatMapEachOnIo();
    }

}



