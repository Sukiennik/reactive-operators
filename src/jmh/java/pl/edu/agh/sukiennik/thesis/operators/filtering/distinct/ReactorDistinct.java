package pl.edu.agh.sukiennik.thesis.operators.filtering.distinct;

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
public class ReactorDistinct {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<Integer> singleDistinct;

    @Setup
    public void setup() {
        singleDistinct = Flux.fromArray(
                IntStream.concat(IntStream.rangeClosed(0, times), IntStream.rangeClosed(0, times)).boxed().toArray(Integer[]::new));
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleDistinct() {
        singleDistinct
                .distinct()
                .then()
                .block();
    }
    
    public static void main(String[] args) {
        //ReactorDistinct distinctBenchmark = new ReactorDistinct();
        //distinctBenchmark.singleDistinct();
    }

}

