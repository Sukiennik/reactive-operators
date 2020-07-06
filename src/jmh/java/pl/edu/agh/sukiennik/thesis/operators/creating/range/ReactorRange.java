package pl.edu.agh.sukiennik.thesis.operators.creating.range;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import reactor.core.publisher.Flux;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class ReactorRange {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;
    
    @Benchmark
    @Measurement(iterations = 5, time = 1)
    public void singleRange() {
        Flux.range(0, times)
                .then()
                .block();
    }

    public static void main(String[] args) {
        //ReactorRange rangeBenchmark = new ReactorRange();
        //rangeBenchmark.singleRange();
    }

}

