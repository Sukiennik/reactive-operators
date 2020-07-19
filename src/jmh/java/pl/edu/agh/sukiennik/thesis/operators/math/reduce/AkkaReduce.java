package pl.edu.agh.sukiennik.thesis.operators.math.reduce;

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
public class AkkaReduce {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Source<Integer, NotUsed> singleReduce;
    private ActorSystem singleReduceSystem;

    @Setup
    public void setup() {
        singleReduce = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
        singleReduceSystem = ActorSystem.create("singleReduceSystem");
    }

    @TearDown
    public void cleanup() {
        singleReduceSystem.terminate();
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleReduce(Blackhole bh) throws ExecutionException, InterruptedException {
        singleReduce
                .reduce(Integer::sum)
                .runWith(Sink.head(), singleReduceSystem)
                .toCompletableFuture()
                .get();
    }

    public static void main(String[] args) {
        //AkkaReduce reduceBenchmark = new AkkaReduce();
        //reduceBenchmark.singleReduce();
    }

}

