package pl.edu.agh.sukiennik.thesis.operators.creating.range;

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
public class AkkaRange {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private ActorSystem singleRangeSystem;

    @Setup
    public void setup() {
        singleRangeSystem = ActorSystem.create("singleRangeSystem");
    }

    @TearDown
    public void cleanup() {
        singleRangeSystem.terminate();
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleRange(Blackhole bh) throws ExecutionException, InterruptedException {
        Source.range(0, times)
                .runWith(Sink.ignore(), singleRangeSystem)
                .toCompletableFuture()
                .get();
    }

    public static void main(String[] args) {
        //AkkaRange rangeBenchmark = new AkkaRange();
        //rangeBenchmark.singleRange();
    }

}

