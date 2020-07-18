package pl.edu.agh.sukiennik.thesis.operators.transforming.flatMap;

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
public class ReactorFlatMap {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<String> characters;
    private Flux<Integer> singleFlatMapFlux;
    private Flux<Integer> multiFlatMapFlux;
    private Flux<Integer> multiFlatMapEachOnIoFlux;

    @Setup
    public void setup() {
        characters = Flux.just("A", "B");
        singleFlatMapFlux = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiFlatMapFlux = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiFlatMapEachOnIoFlux = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }
    
    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleFlatMap() {
        singleFlatMapFlux
                .flatMap(integer -> characters.map(character -> character + integer.toString()), 4)
                .then()
                .block();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiFlatMap() {
        Flux<String> results =  null;
        for (int i = 0; i < 10; i++) {
            if(results == null) {
                results = multiFlatMapFlux.flatMap(integer -> characters.map(character -> character + integer.toString()), 4);
            } else {
                results = results.flatMap(string -> characters.map(character -> character + string), 4);
            }
        }
        results.then().block();
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiFlatMapEachOnIo() {
        Flux<String> results =  null;
        for (int i = 0; i < 10; i++) {
            if(results == null) {
                results = multiFlatMapEachOnIoFlux
                        .publishOn(Schedulers.elastic())
                        .flatMap(integer -> characters.map(character -> character + integer.toString()), 4);
            } else {
                results = results
                        .publishOn(Schedulers.elastic())
                        .flatMap(string -> characters.map(character -> character + string), 4);
            }
        }
        results.then().block();
    }


    public static void main(String[] args) {
        //ReactorFlatMap flatFlatMapBenchmark = new ReactorFlatMap();
        //flatFlatMapBenchmark.setup();
        //flatFlatMapBenchmark.multiFlatMapEachOnIo();
    }

}



