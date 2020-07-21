package pl.edu.agh.sukiennik.thesis.operators.creating.error;

import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.utils.ForcedGcMemoryProfiler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class AkkaError {
    
    private ActorSystem singleErrorSystem;

    @Setup
    public void setup() {
        singleErrorSystem = ActorSystem.create("singleErrorSystem");
    }

    @TearDown
    public void cleanup() {
        singleErrorSystem.terminate();
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleError() throws ExecutionException, InterruptedException {
        Source.failed(new Exception())
                .run(singleErrorSystem)
                .toCompletableFuture()
                .get();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //AkkaError errorBenchmark = new AkkaError();
        //errorBenchmark.setup();
        //errorBenchmark.singleError();
    }

}

