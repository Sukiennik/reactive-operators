package pl.edu.agh.sukiennik.thesis.operators.filtering.first;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import io.reactivex.rxjava3.core.Flowable;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import pl.edu.agh.sukiennik.thesis.operators.PerformanceSubscriber;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class AkkaFirst {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Source<Integer, NotUsed> singleFirst;
    private ActorSystem singleFirstSystem;

    @Setup
    public void setup() {
        singleFirst = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
        singleFirstSystem = ActorSystem.create("singleFirstSystem");
    }

    @TearDown
    public void cleanup() {
        singleFirstSystem.terminate();
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 1)
    public void singleFirst(Blackhole bh) throws ExecutionException, InterruptedException {
        singleFirst
                .runWith(Sink.head(), singleFirstSystem)
                .toCompletableFuture()
                .get();
    }

    public static void main(String[] args) throws InterruptedException {
        //AkkaFirst firstBenchmark = new AkkaFirst();
        //firstBenchmark.singleFirst();
    }

}

