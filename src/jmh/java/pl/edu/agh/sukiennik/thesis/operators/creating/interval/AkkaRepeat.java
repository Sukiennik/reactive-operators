package pl.edu.agh.sukiennik.thesis.operators.creating.interval;

import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.utils.ForcedGcMemoryProfiler;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class AkkaRepeat {

    @Param({"1", "10", "50", "100"})
    private static int times;

    private ActorSystem singleRepeatSystem;

    @Setup
    public void setup() {
        singleRepeatSystem = ActorSystem.create("singleRepeatSystem");
    }

    @TearDown
    public void cleanup() {
        singleRepeatSystem.terminate();
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleRepeat() throws ExecutionException, InterruptedException {
        Source.tick(Duration.ZERO, Duration.ofMillis(25), 1)
                .take(times)
                .run(singleRepeatSystem)
                .toCompletableFuture()
                .get();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //AkkaRepeat repeatBenchmark = new AkkaRepeat();
        //repeatBenchmark.setup();
        //repeatBenchmark.singleRepeat();
    }

}

