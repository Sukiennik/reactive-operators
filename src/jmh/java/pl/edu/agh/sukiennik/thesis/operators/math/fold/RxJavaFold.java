package pl.edu.agh.sukiennik.thesis.operators.math.fold;

import io.reactivex.rxjava3.core.Flowable;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import pl.edu.agh.sukiennik.thesis.operators.PerformanceSubscriber;

import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class RxJavaFold {

    @Param({"1", "1000", "1000000", "10000000"})
    private static long times;

    private Flowable<Long> singleFold;

    @Setup
    public void setup() {
        singleFold = Flowable.fromArray(LongStream.rangeClosed(0, times).boxed().toArray(Long[]::new));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 1)
    public void singleFold(Blackhole bh) {
        singleFold
                .reduce(times, Long::sum)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    public static void main(String[] args) {
        //RxJavaFold foldBenchmark = new RxJavaFold();
        //foldBenchmark.singleFold();
    }

}

