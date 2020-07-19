package pl.edu.agh.sukiennik.thesis.operators.creating.timer;

import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.operators.ForcedGcMemoryProfiler;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class ReactorTimer {

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleTimer() {
        Mono.delay(Duration.ofMillis(1))
                .then()
                .block();
    }

    public static void main(String[] args) {
        //ReactorTimer timerBenchmark = new ReactorTimer();
        //timerBenchmark.singleTimer();
    }

}

