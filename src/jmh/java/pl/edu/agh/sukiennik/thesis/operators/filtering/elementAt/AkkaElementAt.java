package pl.edu.agh.sukiennik.thesis.operators.filtering.elementAt;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.utils.ForcedGcMemoryProfiler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class AkkaElementAt {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Source<Integer, NotUsed> singleElementAt;
    private ActorSystem singleElementAtSystem;

    @Setup
    public void setup() {
        singleElementAt = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
        singleElementAtSystem = ActorSystem.create("singleElementAtSystem");
    }

    @TearDown
    public void cleanup() {
        singleElementAtSystem.terminate();
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleElementAt() throws ExecutionException, InterruptedException {
        singleElementAt
                .zipWithIndex()
                .filter(param -> param.second() == Math.floorDiv(times, 2))
                .run(singleElementAtSystem)
                .toCompletableFuture()
                .get();
    }

    public static void main(String[] args) {
        //AkkaElementAt elementAtBenchmark = new AkkaElementAt();
        //elementAtBenchmark.singleElementAt();
    }
}

