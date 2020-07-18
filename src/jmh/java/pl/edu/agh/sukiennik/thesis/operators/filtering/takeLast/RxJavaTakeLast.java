package pl.edu.agh.sukiennik.thesis.operators.filtering.takeLast;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import pl.edu.agh.sukiennik.thesis.operators.PerformanceSubscriber;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class RxJavaTakeLast {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flowable<Integer> singleTakeLastLastFlowable;
    private Flowable<Integer> multiTakeLastLastFlowable;
    private Flowable<Integer> multiTakeLastLastEachOnIoFlowable;

    @Setup
    public void setup() {
        singleTakeLastLastFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiTakeLastLastFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiTakeLastLastEachOnIoFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleTakeLastLast(Blackhole bh) {
        singleTakeLastLastFlowable
                .takeLast(times / 2)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiTakeLastLast(Blackhole bh) {
        Flowable<Integer> range = multiTakeLastLastFlowable;
        int elements = times;
        for (int i = 0; i < 10; i++) {
            elements = elements / 2;
            int dropCount = elements;
            range = range.takeLast(dropCount);
        }
        range.blockingSubscribe(new PerformanceSubscriber(bh));
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiTakeLastLastEachOnIo(Blackhole bh) {
        Flowable<Integer> range = multiTakeLastLastEachOnIoFlowable;
        int elements = times;
        for (int i = 0; i < 10; i++) {
            elements = elements / 2;
            int dropCount = elements;
            range = range.observeOn(Schedulers.io()).takeLast(dropCount);
        }
        range.blockingSubscribe(new PerformanceSubscriber(bh));
    }

    public static void main(String[] args) {
        //RxJavaTakeLastLast firstBenchmark = new RxJavaTakeLastLast();
        //firstBenchmark.singleTakeLastLast();
    }

}
