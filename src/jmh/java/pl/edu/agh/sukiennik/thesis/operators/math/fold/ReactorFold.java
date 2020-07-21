package pl.edu.agh.sukiennik.thesis.operators.math.fold;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import pl.edu.agh.sukiennik.thesis.utils.ForcedGcMemoryProfiler;
import reactor.core.publisher.Flux;

import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class ReactorFold {

    @Param({"1", "1000", "1000000", "10000000"})
    private static long times;

    private Flux<Long> singleFold;

    @Setup
    public void setup() {
        singleFold = Flux.fromArray(LongStream.rangeClosed(0, times).boxed().toArray(Long[]::new));
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleFold(Blackhole bh) {
        singleFold
                .reduce(times, Long::sum)
                .then()
                .block();
    }

    public static void main(String[] args) {
        //ReactorFold foldBenchmark = new ReactorFold();
        //foldBenchmark.singleFold();
    }

}

