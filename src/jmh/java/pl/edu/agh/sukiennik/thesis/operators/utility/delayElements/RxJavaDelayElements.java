package pl.edu.agh.sukiennik.thesis.operators.utility.delayElements;

import io.reactivex.rxjava3.core.Flowable;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import pl.edu.agh.sukiennik.thesis.utils.ForcedGcMemoryProfiler;
import pl.edu.agh.sukiennik.thesis.utils.PerformanceSubscriber;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class RxJavaDelayElements {

    @Param({"1", "10", "50", "100"})
    private static int times;

    private Flowable<Integer> singleDelayElements;

    @Setup
    public void setup() {
        singleDelayElements = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleDelayElements(Blackhole bh) {
        singleDelayElements
                .zipWith(Flowable.interval(25, TimeUnit.MILLISECONDS), (integer, aLong) -> integer)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    public static void main(String[] args) {
        //RxJavaDelayElements delayElementsBenchmark = new RxJavaDelayElements();
        //delayElementsBenchmark.setup();
        //delayElementsBenchmark.singleDelayElements();
    }

}

