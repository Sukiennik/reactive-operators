package pl.edu.agh.sukiennik.thesis.operators.creating.just;

import akka.actor.ActorSystem;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.utils.ForcedGcMemoryProfiler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class AkkaJust {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private ActorSystem singleJustSystem;

    @Setup
    public void setup() {
        singleJustSystem = ActorSystem.create("singleJustSystem");
    }

    @TearDown
    public void cleanup() {
        singleJustSystem.terminate();
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleJust() throws ExecutionException, InterruptedException {
        Source.single(times)
                .runWith(Sink.head(), singleJustSystem)
                .toCompletableFuture()
                .get();
    }

    public static void main(String[] args) {
        //AkkaJust justBenchmark = new AkkaJust();
        //justBenchmark.singleJust();
    }

}

