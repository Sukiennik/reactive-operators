package pl.edu.agh.sukiennik.thesis.operators.math.count;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Sink;
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
public class AkkaCount {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Source<Long, NotUsed> singleCountSource;
    private ActorSystem singleCountSystem;

    @Setup
    public void setup() {
        singleCountSource = Source.fromJavaStream(() -> LongStream.rangeClosed(0, times));
        singleCountSystem = ActorSystem.create("singleCountSystem");
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @TearDown
    public void cleanup() {
        singleCountSystem.terminate();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleCount() throws ExecutionException, InterruptedException {
        singleCountSource
                .fold(0L, (acc, result) -> acc + 1)
                .runWith(Sink.head(), singleCountSystem)
                .toCompletableFuture()
                .get();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //AkkaCount countBenchmark = new AkkaCount();
        //countBenchmark.setup();
        //countBenchmark.singleCount();
    }
}



