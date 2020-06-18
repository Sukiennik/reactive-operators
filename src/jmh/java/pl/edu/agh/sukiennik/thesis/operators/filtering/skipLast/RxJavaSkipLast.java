package pl.edu.agh.sukiennik.thesis.operators.filtering.skipLast;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import pl.edu.agh.sukiennik.thesis.operators.PerformanceSubscriber;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class RxJavaSkipLast {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flowable<Integer> singleSkipLastFlowable;
    private Flowable<Integer> multiSkipLastFlowable;
    private Flowable<Integer> multiSkipLastEachOnIoFlowable;

    @Setup
    public void setup() {
        singleSkipLastFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiSkipLastFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiSkipLastEachOnIoFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 5)
    public void singleSkipLast(Blackhole bh) {
        singleSkipLastFlowable
                .skipLast(times / 2)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 10)
    public void multiSkipLast(Blackhole bh) {
        Flowable<Integer> range = multiSkipLastFlowable;
        int elements = times;
        for (int i = 0; i < 10; i++) {
            elements = elements / 2;
            int dropCount = elements;
            range = range.skipLast(dropCount);
        }
        range.blockingSubscribe(new PerformanceSubscriber(bh));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiSkipLastEachOnIo(Blackhole bh) {
        Flowable<Integer> range = multiSkipLastEachOnIoFlowable;
        int elements = times;
        for (int i = 0; i < 10; i++) {
            elements = elements / 2;
            int dropCount = elements;
            range = range.observeOn(Schedulers.io()).skipLast(dropCount);
        }
        range.blockingSubscribe(new PerformanceSubscriber(bh));
    }

    public static void main(String[] args) {
        //RxJavaSkipLast firstBenchmark = new RxJavaSkipLast();
        //firstBenchmark.singleSkipLast();
    }

}

