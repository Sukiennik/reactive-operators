package pl.edu.agh.sukiennik.thesis.operators.conditional.skipWhile;

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
public class ReactorSkipWhile {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<Integer> singleSkipWhile;
    private Flux<Integer> multipleSkipWhile;
    private Flux<Integer> multiSkipWhileEachOnIo;

    @Setup
    public void setup() {
        singleSkipWhile = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multipleSkipWhile = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiSkipWhileEachOnIo = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleSkipWhile() {
        singleSkipWhile
                .skipWhile(value -> value <= times / 2)
                .then()
                .block();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiSkipWhile() {
        Flux<Integer> range = multipleSkipWhile;
        int condition = times;
        for (int i = 0; i < 10; i++) {
            condition = condition / 2;
            int finalCondition = condition;
            range = range.skipWhile(value -> value <= finalCondition);
        }
        range.then().block();
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiSkipWhileEachOnIo() {
        Flux<Integer> range = multiSkipWhileEachOnIo;
        int condition = times;
        for (int i = 0; i < 10; i++) {
            condition = condition / 2;
            int finalCondition = condition;
            range = range.publishOn(Schedulers.elastic()).skipWhile(value -> value <= finalCondition);
        }
        range.then().block();
    }

    public static void main(String[] args) {
        //ReactorSkipWhile skipWhileBenchmark = new ReactorSkipWhile();
        //skipWhileBenchmark.singleSkipWhile();
    }

}

