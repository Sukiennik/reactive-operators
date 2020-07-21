package pl.edu.agh.sukiennik.thesis.operators.combining.zip;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import pl.edu.agh.sukiennik.thesis.utils.ForcedGcMemoryProfiler;
import pl.edu.agh.sukiennik.thesis.utils.PerformanceSubscriber;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class RxJavaZip {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flowable<String> singleZipFlowable;
    private Flowable<String> multiZipFlowable;
    private Flowable<String> multiZipEachOnIoFlowable;
    private Flowable<String> zipFlowable;

    @Setup
    public void setup() {
        singleZipFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        multiZipFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        multiZipEachOnIoFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).mapToObj(String::valueOf).toArray(String[]::new));
        zipFlowable = Flowable.fromArray(IntStream.rangeClosed(times, times * 3 / 2).mapToObj(String::valueOf).toArray(String[]::new));
    }

    @TearDown(Level.Iteration)
    public void clear() {
        ForcedGcMemoryProfiler.recordUsedMemory();
        Schedulers.shutdown();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleZip(Blackhole bh) {
        singleZipFlowable
                .zipWith(zipFlowable, String::concat)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiZip(Blackhole bh) {
        Flowable<String> range = multiZipFlowable;
        for (int i = 0; i < 10; i++) {
            range = range.zipWith(zipFlowable, String::concat);
        }
        range.blockingSubscribe(new PerformanceSubscriber(bh));
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiZipEachOnIo(Blackhole bh) {
        Flowable<String> range = multiZipEachOnIoFlowable;
        for (int i = 0; i < 10; i++) {
            range = range.observeOn(Schedulers.io()).zipWith(zipFlowable, String::concat);
        }
        range.blockingSubscribe(new PerformanceSubscriber(bh));
    }


    public static void main(String[] args) {
        //RxJavaZip zipBenchmark = new RxJavaZip();
        //zipBenchmark.setup();
        //zipBenchmark.singleZip();
    }

}

