package pl.edu.agh.sukiennik.thesis.operators.transforming.switchMap;

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
public class ReactorSwitchMap {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<String> characters;
    private Flux<Integer> singleSwitchMapFlux;
    private Flux<Integer> multiSwitchMapFlux;
    private Flux<Integer> multiSwitchMapEachOnIoFlux;

    @Setup
    public void setup() {
        characters = Flux.just("A", "B");
        singleSwitchMapFlux = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiSwitchMapFlux = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiSwitchMapEachOnIoFlux = Flux.fromArray(IntStream.rangeClosed(0, 10000).boxed().toArray(Integer[]::new));
    }
    
    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleSwitchMap() {
        singleSwitchMapFlux
                .switchMap(integer -> characters.map(character -> character + integer.toString()), 4)
                .then()
                .block();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiSwitchMap() {
        Flux<String> results =  null;
        for (int i = 0; i < 10; i++) {
            if(results == null) {
                results = multiSwitchMapFlux.switchMap(integer -> characters.map(character -> character + integer.toString()), 4);
            } else {
                results = results.switchMap(string -> characters.map(character -> character + string), 4);
            }
        }
        results.then().block();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiSwitchMapEachOnIo() {
        Flux<String> results =  null;
        for (int i = 0; i < 10; i++) {
            if(results == null) {
                results = multiSwitchMapEachOnIoFlux
                        .publishOn(Schedulers.elastic())
                        .switchMap(integer -> characters.map(character -> character + integer.toString()), 4);
            } else {
                results = results
                        .publishOn(Schedulers.elastic())
                        .switchMap(string -> characters.map(character -> character + string), 4);
            }
        }
        results.then().block();
    }


    public static void main(String[] args) {
        ReactorSwitchMap flatSwitchMapBenchmark = new ReactorSwitchMap();
        flatSwitchMapBenchmark.setup();
        flatSwitchMapBenchmark.multiSwitchMapEachOnIo();
    }

}



