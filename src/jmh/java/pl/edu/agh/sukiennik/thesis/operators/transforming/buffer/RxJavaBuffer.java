package pl.edu.agh.sukiennik.thesis.operators.transforming.buffer;

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
public class RxJavaBuffer {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flowable<Integer> singleBufferFlowable;
    private Flowable<Integer> multiBufferFlowable;
    private Flowable<Integer> multiBufferEachOnIoFlowable;

    @Setup
    public void setup() {
        singleBufferFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiBufferFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiBufferEachOnIoFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleBuffer(Blackhole bh) {
        singleBufferFlowable
                .buffer(5)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiBuffer(Blackhole bh) {
        multiBufferFlowable
                .buffer(5)
                .buffer(5)
                .buffer(5)
                .buffer(5)
                .buffer(5)
                .buffer(5)
                .buffer(5)
                .buffer(5)
                .buffer(5)
                .buffer(5)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiBufferEachOnIo(Blackhole bh) {
        multiBufferEachOnIoFlowable
                .observeOn(Schedulers.io()).buffer(5)
                .observeOn(Schedulers.io()).buffer(5)
                .observeOn(Schedulers.io()).buffer(5)
                .observeOn(Schedulers.io()).buffer(5)
                .observeOn(Schedulers.io()).buffer(5)
                .observeOn(Schedulers.io()).buffer(5)
                .observeOn(Schedulers.io()).buffer(5)
                .observeOn(Schedulers.io()).buffer(5)
                .observeOn(Schedulers.io()).buffer(5)
                .observeOn(Schedulers.io()).buffer(5)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }


    public static void main(String[] args) {
        //RxJavaBuffer bufferBenchmark = new RxJavaBuffer();
        //bufferBenchmark.singleBuffer();
    }

}

