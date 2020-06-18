package pl.edu.agh.sukiennik.thesis.operators.filtering.last;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
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

    @Benchmark
    @Measurement(iterations = 5, time = 1)
    public void singleLast(Blackhole bh) throws ExecutionException, InterruptedException {
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

