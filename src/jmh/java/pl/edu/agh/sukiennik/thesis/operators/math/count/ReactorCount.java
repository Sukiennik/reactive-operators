package pl.edu.agh.sukiennik.thesis.operators.math.count;

import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.utils.ForcedGcMemoryProfiler;
import reactor.core.publisher.Flux;

import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class ReactorCount {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<Long> singleSumFlux;

    @Setup
    public void setup() {
        singleSumFlux = Flux.fromArray(LongStream.rangeClosed(0, times).boxed().toArray(Long[]::new));
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleSum() {
        singleSumFlux.count().then().block();
    }

    public static void main(String[] args) {
        //ReactorSum sumBenchmark = new ReactorSum();
        //sumBenchmark.singleSum();
    }

}



