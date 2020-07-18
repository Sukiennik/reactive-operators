package pl.edu.agh.sukiennik.thesis.operators.utility.timeout;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.Attributes;
import akka.stream.DelayOverflowStrategy;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class AkkaTimeout {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Source<Integer, NotUsed> singleTimeout;
    private ActorSystem singleTimeoutSystem;

    @Setup
    public void setup() {
        singleTimeout = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
        singleTimeoutSystem = ActorSystem.create("singleTimeoutSystem");
    }

    @TearDown
    public void cleanup() {
        singleTimeoutSystem.terminate();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleTimeout() throws ExecutionException, InterruptedException {
        singleTimeout
                .idleTimeout(Duration.ofMillis(25))
                .run(singleTimeoutSystem)
                .toCompletableFuture()
                .get();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //AkkaTimeout timeoutBenchmark = new AkkaTimeout();
        //timeoutBenchmark.setup();
        //timeoutBenchmark.singleTimeout();
    }
}

