package pl.edu.agh.sukiennik.thesis.operators.transforming.groupBy;

import akka.japi.Pair;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import pl.edu.agh.sukiennik.thesis.operators.PerformanceSubscriber;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class RxJavaGroupBy {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flowable<Integer> singleGroupByFlowable;
    private Flowable<Integer> singleGroupByThenFlattenIndexed;
    private Flowable<Integer> singleGroupByEachOnIoFlowable;

    @Setup
    public void setup() {
        singleGroupByFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        singleGroupByThenFlattenIndexed = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        singleGroupByEachOnIoFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleGroupBy(Blackhole bh) {
        singleGroupByFlowable
                .groupBy(integer -> integer % 5)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleGroupByThenFlattenIndexed(Blackhole bh) {
        singleGroupByThenFlattenIndexed
                .groupBy(integer -> integer % 5)
                .flatMap(integerFlowable ->
                        integerFlowable.zipWith(
                                LongStream.iterate(0, t -> t + 1)::iterator,
                                Pair::create)
                )
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleGroupByOnIo(Blackhole bh) {
        singleGroupByEachOnIoFlowable
                .observeOn(Schedulers.io())
                .groupBy(integer -> integer % 5)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    public static void main(String[] args) {
        //RxJavaGroupBy groupByBenchmark = new RxJavaGroupBy();
        //groupByBenchmark.singleGroupBy();
    }

}

