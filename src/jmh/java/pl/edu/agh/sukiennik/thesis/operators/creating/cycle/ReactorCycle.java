package pl.edu.agh.sukiennik.thesis.operators.creating.cycle;

import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.utils.ForcedGcMemoryProfiler;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class ReactorCycle {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private List<Integer> cycleList;

    @Setup
    public void setup() {
        cycleList = IntStream.range(0, times).boxed().collect(Collectors.toList());
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleCycle() {
        Flux.fromIterable(cycleList).repeat()
                .take(10 * times)
                .then().block();
    }

    public static void main(String[] args) {
        //ReactorCycle cycleBenchmark = new ReactorCycle();
        //cycleBenchmark.setup();
        //cycleBenchmark.singleCycle();
    }

}

