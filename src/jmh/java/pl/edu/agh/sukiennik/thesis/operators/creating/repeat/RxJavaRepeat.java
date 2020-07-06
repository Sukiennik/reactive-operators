package pl.edu.agh.sukiennik.thesis.operators.creating.repeat;

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
public class RxJavaRepeat {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flowable<Integer> singleRepeat;

    @Setup
    public void setup() {
        singleRepeat = Flowable.just(1);
    }

    @Benchmark
    @Measurement(iterations = 5, time = 1)
    public void singleRepeat(Blackhole bh) {
        singleRepeat
                .repeat(times)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    public static void main(String[] args) {
        //RxJavaRepeat repeatBenchmark = new RxJavaRepeat();
        //repeatBenchmark.singleRepeat();
    }

}

