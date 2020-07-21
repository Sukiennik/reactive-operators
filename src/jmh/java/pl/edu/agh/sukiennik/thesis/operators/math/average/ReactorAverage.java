package pl.edu.agh.sukiennik.thesis.operators.math.average;

import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.utils.ForcedGcMemoryProfiler;
import reactor.core.publisher.Flux;
import reactor.math.MathFlux;

import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class ReactorAverage {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<Long> singleAverageFlux;

    @Setup
    public void setup() {
        singleAverageFlux = Flux.fromArray(LongStream.rangeClosed(0, times).boxed().toArray(Long[]::new));
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleAverage() {
        MathFlux.averageBigInteger(singleAverageFlux).then().block();
    }

    public static void main(String[] args) {
        //ReactorAverage averageBenchmark = new ReactorAverage();
        //averageBenchmark.setup();
        //averageBenchmark.singleAverage();
    }

}



