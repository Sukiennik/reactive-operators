package pl.edu.agh.sukiennik.thesis.operators.conditional.takeUntil;

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
public class ReactorTakeUntil {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<Integer> singleTakeUntil;
    private Flux<Integer> multipleTakeUntil;
    private Flux<Integer> multiTakeUntilEachOnIo;

    @Setup
    public void setup() {
        singleTakeUntil = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multipleTakeUntil = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiTakeUntilEachOnIo = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleTakeUntil() {
        singleTakeUntil
                .takeUntil(value -> value > times / 2)
                .then()
                .block();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiTakeUntil() {
        Flux<Integer> range = multipleTakeUntil;
        int condition = times;
        for (int i = 0; i < 10; i++) {
            condition = condition / 2;
            int finalCondition = condition;
            range = range.takeUntil(value -> value > finalCondition);
        }
        range.then().block();
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiTakeUntilEachOnIo() {
        Flux<Integer> range = multiTakeUntilEachOnIo;
        int condition = times;
        for (int i = 0; i < 10; i++) {
            condition = condition / 2;
            int finalCondition = condition;
            range = range.publishOn(Schedulers.elastic()).takeUntil(value -> value > finalCondition);
        }
        range.then().block();
    }

    public static void main(String[] args) {
        //ReactorTakeUntil takeUntilBenchmark = new ReactorTakeUntil();
        //takeUntilBenchmark.singleTakeUntil();
    }

}

