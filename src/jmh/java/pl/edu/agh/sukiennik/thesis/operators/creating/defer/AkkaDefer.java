package pl.edu.agh.sukiennik.thesis.operators.creating.defer;

import akka.actor.ActorSystem;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class AkkaDefer {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private ActorSystem singleDeferSystem;

    @Setup
    public void setup() {
        singleDeferSystem = ActorSystem.create("singleDeferSystem");
    }

    @TearDown
    public void cleanup() {
        singleDeferSystem.terminate();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 1)
    public void singleDefer() throws ExecutionException, InterruptedException {
        Source.lazySource(() -> Source.single(times))
                .runWith(Sink.head(), singleDeferSystem)
                .toCompletableFuture()
                .get();
    }

    public static void main(String[] args) {
        //AkkaDefer deferBenchmark = new AkkaDefer();
        //deferBenchmark.singleDefer();
    }

}

