package pl.edu.agh.sukiennik.thesis.operators.creating.empty;

import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.operators.ForcedGcMemoryProfiler;
import reactor.core.publisher.Flux;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class ReactorEmpty {

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleEmpty() {
        Flux.empty()
                .then()
                .block();
    }

    public static void main(String[] args) {
        //ReactorEmpty emptyBenchmark = new ReactorEmpty();
        //emptyBenchmark.singleEmpty();
    }

}

