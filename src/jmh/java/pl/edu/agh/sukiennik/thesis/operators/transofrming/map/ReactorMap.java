package pl.edu.agh.sukiennik.thesis.operators.transofrming.map;

import org.openjdk.jmh.annotations.*;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class ReactorMap {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    //@Benchmark
    @Measurement(iterations = 5, time = 5)
    public void singleMap() {
        Flux.range(0, times)
                .map(element -> element + 1)
                .then()
                .block();
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 10)
    public void multiMap() {
        Flux<Integer> range = Flux.range(0, times);
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            range = range.map(element -> element + finalI);
        }
        range.then().block();
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiMapEachOnIo() {
        Flux<Integer> range = Flux.range(0, times);
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



