package pl.edu.agh.sukiennik.thesis.operators.creating.repeat;

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
public class AkkaRepeat {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private ActorSystem singleRepeatSystem;

    @Setup
    public void setup() {
        singleRepeatSystem = ActorSystem.create("singleRepeatSystem");
    }

    @TearDown
    public void cleanup() {
        singleRepeatSystem.terminate();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 1)
    public void singleRepeat() throws ExecutionException, InterruptedException {
        Source.repeat(1).take(times)
                .run(singleRepeatSystem)
                .toCompletableFuture()
                .get();
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //AkkaRepeat repeatBenchmark = new AkkaRepeat();
        //repeatBenchmark.setup();
        //repeatBenchmark.singleRepeat();
    }

}

