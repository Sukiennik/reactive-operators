package pl.edu.agh.sukiennik.thesis.operators.creating.empty;

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
public class RxJavaEmpty {

    @Benchmark
    @Measurement(iterations = 5, time = 1)
    public void singleEmpty(Blackhole bh) {
        Flowable.empty()
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    public static void main(String[] args) {
        //RxJavaEmpty emptyBenchmark = new RxJavaEmpty();
        //emptyBenchmark.singleEmpty();
    }

}

