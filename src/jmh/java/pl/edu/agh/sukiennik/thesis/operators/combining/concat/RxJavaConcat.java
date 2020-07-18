package pl.edu.agh.sukiennik.thesis.operators.combining.concat;

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
public class RxJavaConcat {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flowable<String> singleConcatFlowable;
    private Flowable<String> multiConcatFlowable;
    private Flowable<String> multiConcatEachOnIoFlowable;
    private Flowable<String> concatFlowable;

    @Setup
    public void setup() {
        singleConcatFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        multiConcatFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        multiConcatEachOnIoFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        concatFlowable = Flowable.fromArray(IntStream.rangeClosed(times, times * 3 / 2).mapToObj(String::valueOf).toArray(String[]::new));
    }

    @TearDown(Level.Iteration)
    public void clear() {
        Schedulers.shutdown();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleConcat(Blackhole bh) {
        singleConcatFlowable
                .concatWith(concatFlowable)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiConcat(Blackhole bh) {
        Flowable<String> range = multiConcatFlowable;
        for (int i = 0; i < 10; i++) {
            range = range.concatWith(concatFlowable);
        }
        range.blockingSubscribe(new PerformanceSubscriber(bh));
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiConcatEachOnIo(Blackhole bh) {
        Flowable<String> range = multiConcatEachOnIoFlowable;
        for (int i = 0; i < 10; i++) {
            range = range.observeOn(Schedulers.io()).concatWith(concatFlowable);
        }
        range.blockingSubscribe(new PerformanceSubscriber(bh));
    }


    public static void main(String[] args) {
        //RxJavaConcat concatBenchmark = new RxJavaConcat();
        //concatBenchmark.setup();
        //concatBenchmark.singleConcat();
    }

}

