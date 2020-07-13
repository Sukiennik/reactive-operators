package pl.edu.agh.sukiennik.thesis.operators.utility.initialDelay;

import io.reactivex.rxjava3.core.Flowable;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import pl.edu.agh.sukiennik.thesis.operators.PerformanceSubscriber;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class RxJavaInitialDelay {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flowable<Integer> singleInitialDelay;

    @Setup
    public void setup() {
        singleInitialDelay = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 1)
    public void singleInitialDelay(Blackhole bh) {
        singleInitialDelay.delay(25, TimeUnit.MILLISECONDS)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    public static void main(String[] args) {
        //RxJavaInitialDelay initialDelayBenchmark = new RxJavaInitialDelay();
        //initialDelayBenchmark.setup();
        //initialDelayBenchmark.singleInitialDelay();
    }

}

