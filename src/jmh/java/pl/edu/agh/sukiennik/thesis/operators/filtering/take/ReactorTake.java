package pl.edu.agh.sukiennik.thesis.operators.filtering.take;

import org.openjdk.jmh.annotations.*;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class ReactorTake {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<Integer> singleTake;
    private Flux<Integer> multipleTake;
    private Flux<Integer> multiTakeEachOnIo;

    @Setup
    public void setup() {
        singleTake = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multipleTake = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiTakeEachOnIo = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 5)
    public void singleTake() {
        singleTake
                .take(times / 2)
                .then()
                .block();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 10)
    public void multiTake() {
        Flux<Integer> range = multipleTake;
        int elements = times;
        for (int i = 0; i < 10; i++) {
            elements = elements / 2;
            int takeCount = elements;
            range = range.take(takeCount);
        }
        range.then().block();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiTakeEachOnIo() {
        Flux<Integer> range = multiTakeEachOnIo;
        int elements = times;
        for (int i = 0; i < 10; i++) {
            elements = elements / 2;
            int takeCount = elements;
            range = range.publishOn(Schedulers.elastic()).take(takeCount);
        }
        range.then().block();
    }

    public static void main(String[] args) {
        //ReactorTake takeBenchmark = new ReactorTake();
        //takeBenchmark.singleTake();
    }

}

