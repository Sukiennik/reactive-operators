package pl.edu.agh.sukiennik.thesis.operators.creating.timer;

import akka.actor.ActorSystem;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.operators.ForcedGcMemoryProfiler;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class AkkaTimer {

    private ActorSystem singleTimerSystem;

    @Setup
    public void setup() {
        singleTimerSystem = ActorSystem.create("singleTimerSystem");
    }

    @TearDown
    public void cleanup() {
        singleTimerSystem.terminate();
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleTimer() throws ExecutionException, InterruptedException {
        Source.single(0).initialDelay(Duration.ofMillis(1))
                .runWith(Sink.head(), singleTimerSystem)
                .toCompletableFuture()
                .get();
    }

    public static void main(String[] args) {
        //AkkaTimer timerBenchmark = new AkkaTimer();
        //timerBenchmark.singleTimer();
    }

}

