package pl.edu.agh.sukiennik.thesis.operators.combining.merge;

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
public class RxJavaMerge {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flowable<String> singleMergeFlowable;
    private Flowable<String> multiMergeFlowable;
    private Flowable<String> multiMergeEachOnIoFlowable;
    private Flowable<String> mergedFlowable;

    @Setup
    public void setup() {
        singleMergeFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        multiMergeFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        multiMergeEachOnIoFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        mergedFlowable = Flowable.fromArray(IntStream.rangeClosed(times, times * 3 / 2).mapToObj(String::valueOf).toArray(String[]::new));
    }

    @TearDown(Level.Iteration)
    public void clear() {
        Schedulers.shutdown();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleMerge(Blackhole bh) {
        singleMergeFlowable
                .mergeWith(mergedFlowable)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiMerge(Blackhole bh) {
        Flowable<String> range = multiMergeFlowable;
        for (int i = 0; i < 10; i++) {
            range = range.mergeWith(mergedFlowable);
        }
        range.blockingSubscribe(new PerformanceSubscriber(bh));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiMergeEachOnIo(Blackhole bh) {
        Flowable<String> range = multiMergeEachOnIoFlowable;
        for (int i = 0; i < 10; i++) {
            range = range.observeOn(Schedulers.io()).mergeWith(mergedFlowable);
        }
        range.blockingSubscribe(new PerformanceSubscriber(bh));
    }


    public static void main(String[] args) {
        //RxJavaMerge mergeBenchmark = new RxJavaMerge();
        //mergeBenchmark.setup();
        //mergeBenchmark.singleMerge();
    }

}

