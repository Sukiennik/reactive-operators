package pl.edu.agh.sukiennik.thesis.operators.creating.cycle;

import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.utils.ForcedGcMemoryProfiler;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class AkkaCycle {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private ActorSystem singleCycleSystem;
    private List<Integer> cycleList;

    @Setup
    public void setup() {
        cycleList = IntStream.range(0, times).boxed().collect(Collectors.toList());
        singleCycleSystem = ActorSystem.create("singleCycleSystem");
    }

    @TearDown
    public void cleanup() {
        singleCycleSystem.terminate();
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleCycle() throws ExecutionException, InterruptedException {
        Source.cycle(() -> cycleList.iterator())
                .take(10 * times)
                .run(singleCycleSystem)
                .toCompletableFuture()
                .get();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //AkkaCycle cycleBenchmark = new AkkaCycle();
        //cycleBenchmark.setup();
        //cycleBenchmark.singleCycle();
    }

}

