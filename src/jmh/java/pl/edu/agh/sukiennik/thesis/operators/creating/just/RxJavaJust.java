package pl.edu.agh.sukiennik.thesis.operators.creating.just;

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
public class RxJavaJust {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;
    
    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleJust(Blackhole bh) {
        Flowable.just(times)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    public static void main(String[] args) {
        //RxJavaJust justBenchmark = new RxJavaJust();
        //justBenchmark.singleJust();
    }

}
