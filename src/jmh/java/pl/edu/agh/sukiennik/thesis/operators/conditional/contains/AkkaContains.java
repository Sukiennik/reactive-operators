package pl.edu.agh.sukiennik.thesis.operators.conditional.contains;

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
public class AkkaContains {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Source<Integer, NotUsed> singleContains;
    private ActorSystem singleContainsSystem;

    @Setup
    public void setup() {
        singleContains = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
        singleContainsSystem = ActorSystem.create("singleContainsSystem");
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @TearDown
    public void cleanup() {
        singleContainsSystem.terminate();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleContains() throws ExecutionException, InterruptedException {
        int condition = times / 2;
        singleContains
                .fold(false, (predicate, val) -> {
                    if(predicate) {
                        return true;
                    } else {
                        return val == condition;
                    }
                })
                .runWith(Sink.head(), singleContainsSystem)
                .toCompletableFuture()
                .get();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //AkkaContains containsBenchmark = new AkkaContains();
        //containsBenchmark.setup();
        //containsBenchmark.singleContains();
        //containsBenchmark.cleanup();
    }

}

