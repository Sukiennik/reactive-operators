package pl.edu.agh.sukiennik.thesis.operators.math.fold;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class AkkaFold {

    @Param({"1", "1000", "1000000", "10000000"})
    private static long times;

    private Source<Long, NotUsed> singleFold;
    private ActorSystem singleFoldSystem;

    @Setup
    public void setup() {
        singleFold = Source.fromJavaStream(() -> LongStream.rangeClosed(0, times));
        singleFoldSystem = ActorSystem.create("singleFoldSystem");
    }

    @TearDown
    public void cleanup() {
        singleFoldSystem.terminate();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 1)
    public void singleFold(Blackhole bh) throws ExecutionException, InterruptedException {
        singleFold
                .fold(times, Long::sum)
                .runWith(Sink.head(), singleFoldSystem)
                .toCompletableFuture()
                .get();
    }

    public static void main(String[] args) {
        //AkkaFold foldBenchmark = new AkkaFold();
        //foldBenchmark.singleFold();
    }

}

