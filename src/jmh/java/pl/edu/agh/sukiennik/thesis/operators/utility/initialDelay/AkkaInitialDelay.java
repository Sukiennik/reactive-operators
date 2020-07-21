package pl.edu.agh.sukiennik.thesis.operators.utility.initialDelay;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.utils.ForcedGcMemoryProfiler;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class AkkaInitialDelay {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Source<Integer, NotUsed> singleInitialDelay;
    private ActorSystem singleInitialDelaySystem;

    @Setup
    public void setup() {
        singleInitialDelay = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
        singleInitialDelaySystem = ActorSystem.create("singleInitialDelaySystem");
    }

    @TearDown
    public void cleanup() {
        singleInitialDelaySystem.terminate();
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleInitialDelay() throws ExecutionException, InterruptedException {
        singleInitialDelay
                .initialDelay(Duration.ofMillis(25))
                .run(singleInitialDelaySystem)
                .toCompletableFuture()
                .get();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //AkkaInitialDelay initialDelayBenchmark = new AkkaInitialDelay();
        //initialDelayBenchmark.setup();
        //initialDelayBenchmark.singleInitialDelay();
    }
}

