package pl.edu.agh.sukiennik.thesis.operators.utility.delayElements;

import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.utils.ForcedGcMemoryProfiler;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class ReactorDelayElements {

    @Param({"1", "10", "50", "100"})
    private static int times;

    private Flux<Integer> singleDelayElements;

    @Setup
    public void setup() {
        singleDelayElements = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleDelayElements() {
        singleDelayElements
                .delayElements(Duration.ofMillis(25))
                .then()
                .block();
    }

    public static void main(String[] args) {
        //ReactorDelayElements delayElementsBenchmark = new ReactorDelayElements();
        //delayElementsBenchmark.setup();
        //delayElementsBenchmark.singleDelayElements();
    }

}

