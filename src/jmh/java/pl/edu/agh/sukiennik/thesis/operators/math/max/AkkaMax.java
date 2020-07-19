package pl.edu.agh.sukiennik.thesis.operators.math.max;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.operators.ForcedGcMemoryProfiler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class AkkaMax {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Source<Long, NotUsed> singleMaxSource;
    private ActorSystem singleMaxSystem;

    @Setup
    public void setup() {
        singleMaxSource = Source.fromJavaStream(() -> LongStream.rangeClosed(0, times));
        singleMaxSystem = ActorSystem.create("singleMaxSystem");
    }

    @TearDown
    public void cleanup() {
        singleMaxSystem.terminate();
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleMax() throws ExecutionException, InterruptedException {
        singleMaxSource
                .reduce(Long::max)
                .run(singleMaxSystem)
                .toCompletableFuture()
                .get();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //AkkaMax maxBenchmark = new AkkaMax();
        //maxBenchmark.singleMax();
    }
}



