package pl.edu.agh.sukiennik.thesis.operators.conditional.takeWhile;

import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.operators.ForcedGcMemoryProfiler;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class ReactorTakeWhile {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<Integer> singleTakeWhile;
    private Flux<Integer> multipleTakeWhile;
    private Flux<Integer> multiTakeWhileEachOnIo;

    @Setup
    public void setup() {
        singleTakeWhile = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multipleTakeWhile = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiTakeWhileEachOnIo = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleTakeWhile() {
        singleTakeWhile
                .takeWhile(value -> value <= times / 2)
                .then()
                .block();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiTakeWhile() {
        Flux<Integer> range = multipleTakeWhile;
        int condition = times;
        for (int i = 0; i < 10; i++) {
            condition = condition / 2;
            int finalCondition = condition;
            range = range.takeWhile(value -> value <= finalCondition);
        }
        range.then().block();
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiTakeWhileEachOnIo() {
        Flux<Integer> range = multiTakeWhileEachOnIo;
        int condition = times;
        for (int i = 0; i < 10; i++) {
            condition = condition / 2;
            int finalCondition = condition;
            range = range.publishOn(Schedulers.elastic()).takeWhile(value -> value <= finalCondition);
        }
        range.then().block();
    }

    public static void main(String[] args) {
        //ReactortakeWhile takeWhileBenchmark = new ReactortakeWhile();
        //takeWhileBenchmark.singletakeWhile();
    }
}

