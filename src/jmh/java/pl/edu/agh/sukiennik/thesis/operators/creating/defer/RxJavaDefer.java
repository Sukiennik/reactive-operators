package pl.edu.agh.sukiennik.thesis.operators.creating.defer;

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
public class RxJavaDefer {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;
    
    @Benchmark
    @Measurement(iterations = 5, time = 1)
    public void singleDefer(Blackhole bh) {
        Flowable.defer(() -> Flowable.just(times))
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    public static void main(String[] args) {
        //RxJavaDefer deferBenchmark = new RxJavaDefer();
        //deferBenchmark.singleDefer();
    }

}

