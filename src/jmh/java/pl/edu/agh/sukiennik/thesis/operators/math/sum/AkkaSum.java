package pl.edu.agh.sukiennik.thesis.operators.math.sum;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.utils.ForcedGcMemoryProfiler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class AkkaSum {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Source<Long, NotUsed> singleSumSource;
    private ActorSystem singleSumSystem;

    @Setup
    public void setup() {
        singleSumSource = Source.fromJavaStream(() -> LongStream.rangeClosed(0, times));
        singleSumSystem = ActorSystem.create("singleSumSystem");
    }

    @TearDown
    public void cleanup() {
        singleSumSystem.terminate();
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleSum() throws ExecutionException, InterruptedException {
        singleSumSource
                .fold(0L, Long::sum)
                .run(singleSumSystem)
                .toCompletableFuture()
                .get();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //AkkaSum sumBenchmark = new AkkaSum();
        //sumBenchmark.singleSum();
    }
}



