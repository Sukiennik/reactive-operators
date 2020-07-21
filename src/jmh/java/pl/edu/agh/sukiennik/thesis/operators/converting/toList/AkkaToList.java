package pl.edu.agh.sukiennik.thesis.operators.converting.toList;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Sink;
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
public class AkkaToList {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Source<Integer, NotUsed> singleToList;
    private ActorSystem singleToListSystem;

    @Setup
    public void setup() {
        singleToList = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
        singleToListSystem = ActorSystem.create("singleToListSystem");
    }

    @TearDown
    public void cleanup() {
        singleToListSystem.terminate();
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleToList() throws ExecutionException, InterruptedException {
        singleToList
                .runWith(Sink.seq(), singleToListSystem)
                .toCompletableFuture()
                .get();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //AkkaToList toListBenchmark = new AkkaToList();
        //toListBenchmark.setup();
        //toListBenchmark.singleToList();
    }
}

