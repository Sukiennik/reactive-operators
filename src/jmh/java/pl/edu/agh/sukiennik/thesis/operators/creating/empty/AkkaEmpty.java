package pl.edu.agh.sukiennik.thesis.operators.creating.empty;

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
public class AkkaEmpty {
    
    private ActorSystem singleEmptySystem;

    @Setup
    public void setup() {
        singleEmptySystem = ActorSystem.create("singleEmptySystem");
    }

    @TearDown
    public void cleanup() {
        singleEmptySystem.terminate();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 1)
    public void singleEmpty() throws ExecutionException, InterruptedException {
        Source.empty()
                .run(singleEmptySystem)
                .toCompletableFuture()
                .get();
    }

    public static void main(String[] args) {
        //AkkaEmpty emptyBenchmark = new AkkaEmpty();
        //emptyBenchmark.singleEmpty();
    }

}

