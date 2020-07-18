package pl.edu.agh.sukiennik.thesis.operators.filtering.take;

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
public class RxJavaTake {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flowable<Integer> singleTakeFlowable;
    private Flowable<Integer> multiTakeFlowable;
    private Flowable<Integer> multiTakeEachOnIoFlowable;

    @Setup
    public void setup() {
        singleTakeFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiTakeFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiTakeEachOnIoFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleTake(Blackhole bh) {
        singleTakeFlowable
                .take(times / 2)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiTake(Blackhole bh) {
        Flowable<Integer> range = multiTakeFlowable;
        int elements = times;
        for (int i = 0; i < 10; i++) {
            elements = elements / 2;
            int dropCount = elements;
            range = range.take(dropCount);
        }
        range.blockingSubscribe(new PerformanceSubscriber(bh));
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiTakeEachOnIo(Blackhole bh) {
        Flowable<Integer> range = multiTakeEachOnIoFlowable;
        int elements = times;
        for (int i = 0; i < 10; i++) {
            elements = elements / 2;
            int dropCount = elements;
            range = range.observeOn(Schedulers.io()).take(dropCount);
        }
        range.blockingSubscribe(new PerformanceSubscriber(bh));
    }

    public static void main(String[] args) {
        //RxJavaTake firstBenchmark = new RxJavaTake();
        //firstBenchmark.singleTake();
    }

}
