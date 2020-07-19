package pl.edu.agh.sukiennik.thesis.operators.transforming.switchMap;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import pl.edu.agh.sukiennik.thesis.operators.ForcedGcMemoryProfiler;
import pl.edu.agh.sukiennik.thesis.operators.PerformanceSubscriber;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class RxJavaSwitchMap {

    @Param({"1", "1000", "100000", "1000000"})
    private static int times;

    private Flowable<String> characters;
    private Flowable<Integer> singleSwitchMapFlowable;
    private Flowable<Integer> multiSwitchMapFlowable;
    private Flowable<Integer> multiSwitchMapEachOnIoFlowable;

    @Setup
    public void setup() {
        characters = Flowable.just("A", "B");
        singleSwitchMapFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiSwitchMapFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiSwitchMapEachOnIoFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }
    
    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleSwitchMap(Blackhole bh) {
        singleSwitchMapFlowable
                .switchMap(integer -> characters.map(character -> character + integer.toString()))
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiSwitchMap(Blackhole bh) {
        Flowable<String> results = null;
        for (int i = 0; i < 10; i++) {
            if(results == null) {
                results = multiSwitchMapFlowable.switchMap(integer -> characters.map(character -> character + integer.toString()));
            } else {
                results = results.switchMap(string -> characters.map(character -> character + string));
            }
        }
        results.blockingSubscribe(new PerformanceSubscriber(bh));
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiSwitchMapEachOnIo(Blackhole bh) {
        Flowable<String> results = null;
        for (int i = 0; i < 10; i++) {
            if(results == null) {
                results = multiSwitchMapEachOnIoFlowable
                        .observeOn(Schedulers.io())
                        .switchMap(integer -> characters.map(character -> character + integer.toString()));
            } else {
                results = results
                        .observeOn(Schedulers.io())
                        .switchMap(string -> characters.map(character -> character + string));
            }
        }
        results.blockingSubscribe(new PerformanceSubscriber(bh));
    }


    public static void main(String[] args) {
        //RxJavaSwitchMap switchMapBenchmark = new RxJavaSwitchMap();
        //switchMapBenchmark.singleSwitchMap();
    }

}

