package pl.edu.agh.sukiennik.thesis.operators.math.average;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.Pair;
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
public class AkkaAverage {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Source<Long, NotUsed> singleAverageSource;
    private ActorSystem singleAverageSystem;

    @Setup
    public void setup() {
        singleAverageSource = Source.fromJavaStream(() -> LongStream.rangeClosed(0, times));
        singleAverageSystem = ActorSystem.create("singleAverageSystem");
    }

    @TearDown
    public void cleanup() {
        singleAverageSystem.terminate();
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleAverage() throws ExecutionException, InterruptedException {
        singleAverageSource
                .zipWithIndex()
                .reduce((arg1, arg2) ->  Pair.create(arg1.first() + arg2.first(), arg2.second()))
                .map(param -> param.first() / param.second())
                .runWith(Sink.head(), singleAverageSystem)
                .toCompletableFuture()
                .get();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        AkkaAverage sumBenchmark = new AkkaAverage();
        sumBenchmark.singleAverage();
    }

    /*
      No average operation in AkkaStreams (used with others)
     */
}



