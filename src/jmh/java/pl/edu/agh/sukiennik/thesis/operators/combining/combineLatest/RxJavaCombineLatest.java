package pl.edu.agh.sukiennik.thesis.operators.combining.combineLatest;

import akka.stream.javadsl.Flow;
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
public class RxJavaCombineLatest {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flowable<String> singleCombineLatestFlowable;
    private Flowable<String> multiCombineLatestFlowable;
    private Flowable<String> multiCombineLatestEachOnIoFlowable;
    private Flowable<String> combineLatestFlowable;

    @Setup
    public void setup() {
        singleCombineLatestFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        multiCombineLatestFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        multiCombineLatestEachOnIoFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        combineLatestFlowable = Flowable.fromArray(IntStream.rangeClosed(times, times * 3 / 2).mapToObj(String::valueOf).toArray(String[]::new));
    }

    @TearDown(Level.Iteration)
    public void clear() {
        Schedulers.shutdown();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleCombineLatest(Blackhole bh) {
        Flowable.combineLatest(singleCombineLatestFlowable, combineLatestFlowable, String::concat)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiCombineLatest(Blackhole bh) {
        Flowable<String> range = multiCombineLatestFlowable;
        for (int i = 0; i < 10; i++) {
            range = Flowable.combineLatest(range, combineLatestFlowable, String::concat);
        }
        range.blockingSubscribe(new PerformanceSubscriber(bh));
    }

//    @Benchmark
//    @Measurement(iterations = 5, time = 20)
//    public void multiCombineLatestEachOnIo(Blackhole bh) {
//        Flowable<String> range = multiCombineLatestEachOnIoFlowable;
//        for (int i = 0; i < 10; i++) {
//            range = Flowable.combineLatest(range.observeOn(Schedulers.io()), combineLatestFlowable.observeOn(Schedulers.io()), String::concat);
//        }
//        range.blockingSubscribe(new PerformanceSubscriber(bh));
//    }


    public static void main(String[] args) {
        //RxJavaCombineLatest combineLatestBenchmark = new RxJavaCombineLatest();
        //combineLatestBenchmark.setup();
        //combineLatestBenchmark.singleCombineLatest();
    }

}

