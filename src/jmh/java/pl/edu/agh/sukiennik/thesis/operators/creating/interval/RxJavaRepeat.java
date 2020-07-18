package pl.edu.agh.sukiennik.thesis.operators.creating.interval;

import io.reactivex.rxjava3.core.Flowable;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import pl.edu.agh.sukiennik.thesis.operators.PerformanceSubscriber;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class RxJavaRepeat {

    @Param({"1", "10", "50", "100"})
    private static int times;

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleRepeat(Blackhole bh) {
        Flowable.intervalRange(0, times, 0, 25, TimeUnit.MILLISECONDS)
                .onBackpressureBuffer()
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    public static void main(String[] args) {
        //RxJavaRepeat repeatBenchmark = new RxJavaRepeat();
        //repeatBenchmark.singleRepeat();
    }

}

