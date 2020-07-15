package pl.edu.agh.sukiennik.thesis.operators.converting.toList;

import org.openjdk.jmh.annotations.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class ReactorToList {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<Integer> singleToList;

    @Setup
    public void setup() {
        singleToList = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 1)
    public void singleToList() {
        singleToList.collectList()
                .then()
                .block();
    }

    public static void main(String[] args) {
        //ReactorToList toListBenchmark = new ReactorToList();
        //toListBenchmark.setup();
        //toListBenchmark.singleToList();
    }

}

