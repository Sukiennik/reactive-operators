package pl.edu.agh.sukiennik.thesis.operators.utility.delayElements;

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

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class AkkaDelayElements {

    @Param({"1", "10", "50", "100"})
    private static int times;

    private Source<Integer, NotUsed> singleDelayElements;
    private ActorSystem singleDelayElementsSystem;

    @Setup
    public void setup() {
        singleDelayElements = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
        singleDelayElementsSystem = ActorSystem.create("singleDelayElementsSystem");
    }

    @TearDown
    public void cleanup() {
        singleDelayElementsSystem.terminate();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 1)
    public void singleDelayElements() throws ExecutionException, InterruptedException {
        singleDelayElements
                .delay(Duration.ofMillis(25), DelayOverflowStrategy.backpressure())
                .withAttributes(Attributes.inputBuffer(1, 1))
                .runForeach(System.out::println, singleDelayElementsSystem)
                .toCompletableFuture()
                .get();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //AkkaDelayElements delayElementsBenchmark = new AkkaDelayElements();
        //delayElementsBenchmark.setup();
        //delayElementsBenchmark.singleDelayElements();
    }
}

