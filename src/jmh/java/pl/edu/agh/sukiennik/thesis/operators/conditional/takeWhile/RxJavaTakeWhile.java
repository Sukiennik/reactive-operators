package pl.edu.agh.sukiennik.thesis.operators.conditional.takeWhile;

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
public class RxJavaTakeWhile {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flowable<Integer> singleTakeWhileFlowable;
    private Flowable<Integer> multiTakeWhileFlowable;
    private Flowable<Integer> multiTakeWhileEachOnIoFlowable;

    @Setup
    public void setup() {
        singleTakeWhileFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiTakeWhileFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiTakeWhileEachOnIoFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleTakeWhile(Blackhole bh) {
        singleTakeWhileFlowable
                .takeWhile(value -> value <= times / 2)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiTakeWhile(Blackhole bh) {
        Flowable<Integer> range = multiTakeWhileFlowable;
        int condition = times;
        for (int i = 0; i < 10; i++) {
            condition = condition / 2;
            int finalCondition = condition;
            range = range.takeWhile(value -> value <= finalCondition);
        }
        range.blockingSubscribe(new PerformanceSubscriber(bh));
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiTakeWhileEachOnIo(Blackhole bh) {
        Flowable<Integer> range = multiTakeWhileEachOnIoFlowable;
        int condition = times;
        for (int i = 0; i < 10; i++) {
            condition = condition / 2;
            int finalCondition = condition;
            range = range.observeOn(Schedulers.io()).takeWhile(value -> value <= finalCondition);
        }
        range.blockingSubscribe(new PerformanceSubscriber(bh));
    }

    public static void main(String[] args) {
        //RxJavatakeWhile firstBenchmark = new RxJavatakeWhile();
        //firstBenchmark.singletakeWhile();
    }
}

