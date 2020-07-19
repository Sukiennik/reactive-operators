package pl.edu.agh.sukiennik.thesis.operators.math.max;

import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.operators.ForcedGcMemoryProfiler;
import reactor.core.publisher.Flux;
import reactor.math.MathFlux;

import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class ReactorMax {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<Long> singleMaxFlux;

    @Setup
    public void setup() {
        singleMaxFlux = Flux.fromArray(LongStream.rangeClosed(0, times).boxed().toArray(Long[]::new));
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleMax() {
        MathFlux.max(singleMaxFlux).then().block();
    }

    public static void main(String[] args) {
        //ReactorMax maxBenchmark = new ReactorMax();
        //maxBenchmark.singleMax();
    }

}



