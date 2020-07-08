package pl.edu.agh.sukiennik.thesis.operators.creating.timer;

import io.reactivex.rxjava3.core.Flowable;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import pl.edu.agh.sukiennik.thesis.operators.PerformanceSubscriber;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class RxJavaTimer {

    @Benchmark
    @Measurement(iterations = 5, time = 1)
    public void singleTimer(Blackhole bh) {
        Flowable.timer(1, TimeUnit.MILLISECONDS)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    public static void main(String[] args) {
        //RxJavaTimer timerBenchmark = new RxJavaTimer();
        //timerBenchmark.singleTimer();
    }

}

