package pl.edu.agh.sukiennik.thesis.operators.conditional.contains;

import org.openjdk.jmh.annotations.*;
import reactor.core.publisher.Flux;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class ReactorContains {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<Integer> singleContains;

    @Setup
    public void setup() {
        singleContains = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 1)
    public void singleContains() {
        int condition = times / 2;
        singleContains
                .hasElement(condition)
                .then()
                .block();
    }

    public static void main(String[] args) {
        //ReactorContains containsBenchmark = new ReactorContains();
        //containsBenchmark.singleContains();
    }

}

