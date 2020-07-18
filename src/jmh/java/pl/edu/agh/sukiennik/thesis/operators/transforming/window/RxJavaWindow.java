package pl.edu.agh.sukiennik.thesis.operators.transforming.window;

import akka.japi.Pair;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import pl.edu.agh.sukiennik.thesis.operators.PerformanceSubscriber;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class RxJavaWindow {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flowable<Integer> singleWindowFlowable;
    private Flowable<Integer> singleWindowThenFlattenIndexedFlowable;
    private Flowable<Integer> multiWindowFlowable;
    private Flowable<Integer> multiWindowEachOnIoFlowable;

    @Setup
    public void setup() {
        singleWindowFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        singleWindowThenFlattenIndexedFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiWindowFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiWindowEachOnIoFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleWindow(Blackhole bh) {
        singleWindowFlowable
                .window(5)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleWindowThenFlattenIndexed(Blackhole bh) {
        singleWindowThenFlattenIndexedFlowable
                .window(5)
                .flatMap(integerFlowable ->
                        integerFlowable.zipWith(
                                LongStream.iterate(0, t -> t + 1)::iterator,
                                Pair::create)
                )
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiWindow(Blackhole bh) {
        multiWindowFlowable
                .window(5)
                .window(5)
                .window(5)
                .window(5)
                .window(5)
                .window(5)
                .window(5)
                .window(5)
                .window(5)
                .window(5)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiWindowEachOnIo(Blackhole bh) {
        multiWindowEachOnIoFlowable
                .observeOn(Schedulers.io()).window(5)
                .observeOn(Schedulers.io()).window(5)
                .observeOn(Schedulers.io()).window(5)
                .observeOn(Schedulers.io()).window(5)
                .observeOn(Schedulers.io()).window(5)
                .observeOn(Schedulers.io()).window(5)
                .observeOn(Schedulers.io()).window(5)
                .observeOn(Schedulers.io()).window(5)
                .observeOn(Schedulers.io()).window(5)
                .observeOn(Schedulers.io()).window(5)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }


    public static void main(String[] args) {
        //RxJavaWindow windowBenchmark = new RxJavaWindow();
        //windowBenchmark.setup();
        //windowBenchmark.singleWindow();
    }

}

