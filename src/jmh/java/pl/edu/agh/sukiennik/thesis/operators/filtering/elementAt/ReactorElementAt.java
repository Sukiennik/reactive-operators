package pl.edu.agh.sukiennik.thesis.operators.filtering.elementAt;

import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.utils.ForcedGcMemoryProfiler;
import reactor.core.publisher.Flux;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class ReactorElementAt {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<Integer> singleElementAt;

    @Setup
    public void setup() {
        singleElementAt = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleElementAt() {
        singleElementAt
                .elementAt(Math.floorDiv(times, 2))
                .block();
    }

    public static void main(String[] args) {
        //ReactorElementAt elementAtBenchmark = new ReactorElementAt();
        //elementAtBenchmark.singleElementAt();
    }

}

