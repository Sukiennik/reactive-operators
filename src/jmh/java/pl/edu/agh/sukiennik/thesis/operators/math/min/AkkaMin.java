package pl.edu.agh.sukiennik.thesis.operators.math.min;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class AkkaMin {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Source<Long, NotUsed> singleMinSource;
    private ActorSystem singleMinSystem;

    @Setup
    public void setup() {
        singleMinSource = Source.fromJavaStream(() -> LongStream.rangeClosed(0, times));
        singleMinSystem = ActorSystem.create("singleMinSystem");
    }

    @TearDown
    public void cleanup() {
        singleMinSystem.terminate();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleMin() throws ExecutionException, InterruptedException {
        singleMinSource
                .reduce(Long::min)
                .run(singleMinSystem)
                .toCompletableFuture()
                .get();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //AkkaMin minBenchmark = new AkkaMin();
        //minBenchmark.singleMin();
    }
}



