package pl.edu.agh.sukiennik.thesis.operators.utility.doOnComplete;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.utils.ForcedGcMemoryProfiler;
import scala.util.Try;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class AkkaDoOnComplete {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Source<Integer, NotUsed> singleDoOnComplete;
    private ActorSystem singleDoOnCompleteSystem;

    @Setup
    public void setup() {
        singleDoOnComplete = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
        singleDoOnCompleteSystem = ActorSystem.create("singleDoOnCompleteSystem");
    }

    @TearDown
    public void cleanup() {
        singleDoOnCompleteSystem.terminate();
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleDoOnComplete() throws ExecutionException, InterruptedException {
        singleDoOnComplete
                .runWith(Sink.onComplete(Try::isSuccess), singleDoOnCompleteSystem);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //AkkaDoOnComplete doOnCompleteBenchmark = new AkkaDoOnComplete();
        //doOnCompleteBenchmark.setup();
        //doOnCompleteBenchmark.singleDoOnComplete();
    }
}

