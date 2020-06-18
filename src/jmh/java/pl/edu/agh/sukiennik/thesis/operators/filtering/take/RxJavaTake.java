package pl.edu.agh.sukiennik.thesis.operators.filtering.take;

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
public class RxJavaTake {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flowable<Integer> singleTakeLastFlowable;
    private Flowable<Integer> multiTakeLastFlowable;
    private Flowable<Integer> multiTakeLastEachOnIoFlowable;

    @Setup
    public void setup() {
        singleTakeLastFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiTakeLastFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiTakeLastEachOnIoFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 5)
    public void singleTakeLast(Blackhole bh) {
        singleTakeLastFlowable
                .takeLast(times / 2)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 10)
    public void multiTakeLast(Blackhole bh) {
        Flowable<Integer> range = multiTakeLastFlowable;
        int elements = times;
        for (int i = 0; i < 10; i++) {
            elements = elements / 2;
            int dropCount = elements;
            range = range.take(dropCount);
        }
        range.blockingSubscribe(new PerformanceSubscriber(bh));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiTakeLastEachOnIo(Blackhole bh) {
        Flowable<Integer> range = multiTakeLastEachOnIoFlowable;
        int elements = times;
        for (int i = 0; i < 10; i++) {
            elements = elements / 2;
            int dropCount = elements;
            range = range.observeOn(Schedulers.io()).take(dropCount);
        }
        range.blockingSubscribe(new PerformanceSubscriber(bh));
    }

    public static void main(String[] args) {
        //RxJavaTakeLast firstBenchmark = new RxJavaTakeLast();
        //firstBenchmark.singleTakeLast();
    }

}
