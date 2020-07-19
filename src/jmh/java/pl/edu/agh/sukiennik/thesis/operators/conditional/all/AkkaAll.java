package pl.edu.agh.sukiennik.thesis.operators.conditional.all;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import pl.edu.agh.sukiennik.thesis.operators.ForcedGcMemoryProfiler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class AkkaAll {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Source<Integer, NotUsed> singleAll;
    private ActorSystem singleAllSystem;

    @Setup
    public void setup() {
        singleAll = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
        singleAllSystem = ActorSystem.create("singleAllSystem");
    }

    @TearDown
    public void cleanup() {
        singleAllSystem.terminate();
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleAll() throws ExecutionException, InterruptedException {
        int condition = times / 2;
        singleAll
                .fold(true, (predicate, arg2) -> {
                    if(!predicate) {
                        return false;
                    } else {
                        return arg2 < condition;
                    }
                })
                .runWith(Sink.head(), singleAllSystem)
                .toCompletableFuture()
                .get();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        AkkaAll allBenchmark = new AkkaAll();
        allBenchmark.setup();
        allBenchmark.singleAll();
    }

}

