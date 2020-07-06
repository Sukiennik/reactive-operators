package pl.edu.agh.sukiennik.thesis.operators.combining.startWith;

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
public class RxJavaStartWith {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flowable<String> singleStartWithFlowable;
    private Flowable<String> multiStartWithFlowable;
    private Flowable<String> multiStartWithEachOnIoFlowable;
    private Flowable<String> startWithFlowable;

    @Setup
    public void setup() {
        singleStartWithFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        multiStartWithFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        multiStartWithEachOnIoFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        startWithFlowable = Flowable.fromArray(IntStream.rangeClosed(times, times * 3 / 2).mapToObj(String::valueOf).toArray(String[]::new));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 5)
    public void singleStartWith(Blackhole bh) {
        singleStartWithFlowable
                .startWith(startWithFlowable)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 10)
    public void multiStartWith(Blackhole bh) {
        Flowable<String> range = multiStartWithFlowable;
        for (int i = 0; i < 10; i++) {
            range = range.startWith(startWithFlowable);
        }
        range.blockingSubscribe(new PerformanceSubscriber(bh));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiStartWithEachOnIo(Blackhole bh) {
        Flowable<String> range = multiStartWithEachOnIoFlowable;
        for (int i = 0; i < 10; i++) {
            range = range.observeOn(Schedulers.io()).startWith(startWithFlowable);
        }
        range.blockingSubscribe(new PerformanceSubscriber(bh));
    }


    public static void main(String[] args) {
        //RxJavaStartWith startWithBenchmark = new RxJavaStartWith();
        //startWithBenchmark.setup();
        //startWithBenchmark.singleStartWith();
    }

}

