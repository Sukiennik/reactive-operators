package pl.edu.agh.sukiennik.thesis.operators.conditional.skipUntil;

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
public class ReactorSkipUntil {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<Integer> singleSkipUntil;
    private Flux<Integer> multipleSkipUntil;
    private Flux<Integer> multiSkipUntilEachOnIo;

    @Setup
    public void setup() {
        singleSkipUntil = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multipleSkipUntil = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiSkipUntilEachOnIo = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleSkipUntil() {
        singleSkipUntil
                .skipUntil(value -> value > times / 2)
                .then()
                .block();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiSkipUntil() {
        Flux<Integer> range = multipleSkipUntil;
        int condition = times;
        for (int i = 0; i < 10; i++) {
            condition = condition / 2;
            int finalCondition = condition;
            range = range.skipUntil(value -> value > finalCondition);
        }
        range.then().block();
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiSkipUntilEachOnIo() {
        Flux<Integer> range = multiSkipUntilEachOnIo;
        int condition = times;
        for (int i = 0; i < 10; i++) {
            condition = condition / 2;
            int finalCondition = condition;
            range = range.publishOn(Schedulers.elastic()).skipUntil(value -> value > finalCondition);
        }
        range.then().block();
    }

    public static void main(String[] args) {
        //ReactorSkipUntil skipUntilBenchmark = new ReactorSkipUntil();
        //skipUntilBenchmark.singleSkipUntil();
    }

}

