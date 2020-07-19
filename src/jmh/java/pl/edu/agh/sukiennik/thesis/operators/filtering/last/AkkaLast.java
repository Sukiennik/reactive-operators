package pl.edu.agh.sukiennik.thesis.operators.filtering.last;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.operators.ForcedGcMemoryProfiler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class AkkaLast {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Source<Integer, NotUsed> singleLast;
    private ActorSystem singleLastSystem;

    @Setup
    public void setup() {
        singleLast = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
        singleLastSystem = ActorSystem.create("singleLastSystem");
    }

    @TearDown
    public void cleanup() {
        singleLastSystem.terminate();
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleLast() throws ExecutionException, InterruptedException {
        singleLast
                .runWith(Sink.last(), singleLastSystem)
                .toCompletableFuture()
                .get();
    }

    public static void main(String[] args) {
        //AkkaLast lastBenchmark = new AkkaLast();
        //lastBenchmark.singleLast();
    }
}

