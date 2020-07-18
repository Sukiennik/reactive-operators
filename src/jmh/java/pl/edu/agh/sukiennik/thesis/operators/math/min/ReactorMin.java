package pl.edu.agh.sukiennik.thesis.operators.math.min;

import org.openjdk.jmh.annotations.*;
import reactor.core.publisher.Flux;
import reactor.math.MathFlux;

import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class ReactorMin {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<Long> singleMinFlux;

    @Setup
    public void setup() {
        singleMinFlux = Flux.fromArray(LongStream.rangeClosed(0, times).boxed().toArray(Long[]::new));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleMin() {
        MathFlux.min(singleMinFlux).then().block();
    }

    public static void main(String[] args) {
        //ReactorMin minBenchmark = new ReactorMin();
        //minBenchmark.singleMin();
    }

}



