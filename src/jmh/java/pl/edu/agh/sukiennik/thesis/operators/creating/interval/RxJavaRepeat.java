package pl.edu.agh.sukiennik.thesis.operators.creating.interval;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import pl.edu.agh.sukiennik.thesis.operators.PerformanceSubscriber;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class RxJavaRepeat {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    @Benchmark
    @Measurement(iterations = 5, time = 1)
    public void singleRepeat(Blackhole bh) {
        Flowable.intervalRange(0, times, 0, 1, TimeUnit.MILLISECONDS)
                .map(aLong -> 1)
                .onBackpressureBuffer()
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    public static void main(String[] args) {
        //RxJavaRepeat repeatBenchmark = new RxJavaRepeat();
        //repeatBenchmark.singleRepeat();
    }

}

