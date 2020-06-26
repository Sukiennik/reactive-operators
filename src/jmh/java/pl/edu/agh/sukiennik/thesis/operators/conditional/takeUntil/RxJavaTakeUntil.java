package pl.edu.agh.sukiennik.thesis.operators.conditional.takeUntil;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
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
public class RxJavaTakeUntil {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flowable<Integer> singleTakeUntilFlowable;
    private Flowable<Integer> multiTakeUntilFlowable;
    private Flowable<Integer> multiTakeUntilEachOnIoFlowable;

    @Setup
    public void setup() {
        singleTakeUntilFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiTakeUntilFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiTakeUntilEachOnIoFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 5)
    public void singleTakeUntil(Blackhole bh) {
        singleTakeUntilFlowable
                .takeUntil(value -> value > times / 2)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 10)
    public void multiTakeUntil(Blackhole bh) {
        Flowable<Integer> range = multiTakeUntilFlowable;
        int condition = times;
        for (int i = 0; i < 10; i++) {
            condition = condition / 2;
            int finalCondition = condition;
            range = range.takeUntil(value -> value > finalCondition);
        }
        range.blockingSubscribe(new PerformanceSubscriber(bh));
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiTakeUntilEachOnIo(Blackhole bh) {
        Flowable<Integer> range = multiTakeUntilEachOnIoFlowable;
        int condition = times;
        for (int i = 0; i < 10; i++) {
            condition = condition / 2;
            int finalCondition = condition;
            range = range.observeOn(Schedulers.io()).takeUntil(value -> value > finalCondition);
        }
        range.blockingSubscribe(new PerformanceSubscriber(bh));
    }

    public static void main(String[] args) {
        //RxJavaTakeUntil firstBenchmark = new RxJavaTakeUntil();
        //firstBenchmark.singleTakeUntil();
    }
}

