package pl.edu.agh.sukiennik.thesis.operators.filtering.skipLast;

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
public class ReactorSkipLast {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<Integer> singleSkipLast;
    private Flux<Integer> multipleSkipLast;
    private Flux<Integer> multiSkipLastEachOnIo;

    @Setup
    public void setup() {
        singleSkipLast = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multipleSkipLast = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiSkipLastEachOnIo = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 5)
    public void singleSkipLast() {
        singleSkipLast
                .skipLast(times / 2)
                .then()
                .block();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 10)
    public void multiSkipLast() {
        Flux<Integer> range = multipleSkipLast;
        int condition = times;
        for (int i = 0; i < 10; i++) {
            condition = condition / 2;
            int finalCondition = condition;
            range = range.skipLast(finalCondition);
        }
        range.then().block();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiSkipLastEachOnIo() {
        Flux<Integer> range = multiSkipLastEachOnIo;
        int drop = times;
        for (int i = 0; i < 10; i++) {
            drop = drop / 2;
            int dropCount = drop;
            range = range.publishOn(Schedulers.elastic()).skipLast(dropCount);
        }
        range.then().block();
    }

    public static void main(String[] args) {
        //ReactorSkipLast skipLastBenchmark = new ReactorSkipLast();
        //skipLastBenchmark.singleSkipLast();
    }

}

