package pl.edu.agh.sukiennik.thesis.operators.transforming.flatMap;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import pl.edu.agh.sukiennik.thesis.operators.PerformanceSubscriber;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class RxJavaFlatMap {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flowable<String> characters;
    private Flowable<Integer> singleFlatMapFlowable;
    private Flowable<Integer> multiFlatMapFlowable;
    private Flowable<Integer> multiFlatMapEachOnIoFlowable;

    @Setup
    public void setup() {
        characters = Flowable.just("A", "B");
        singleFlatMapFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiFlatMapFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiFlatMapEachOnIoFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }
    
    @Benchmark
    @Measurement(iterations = 5, time = 5)
    public void singleFlatMap(Blackhole bh) {
        singleFlatMapFlowable
                .flatMap(integer -> characters.map(character -> character + integer.toString()), 4)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 10)
    public void multiFlatMap(Blackhole bh) {
        Flowable<String> results = null;
        for (int i = 0; i < 10; i++) {
            if(results == null) {
                results = multiFlatMapFlowable.flatMap(integer -> characters.map(character -> character + integer.toString()), 4);
            } else {
                results = results.flatMap(string -> characters.map(character -> character + string), 4);
            }
        }
        results.blockingSubscribe(new PerformanceSubscriber(bh));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiFlatMapEachOnIo(Blackhole bh) {
        Flowable<String> results = null;
        for (int i = 0; i < 10; i++) {
            if(results == null) {
                results = multiFlatMapEachOnIoFlowable
                        .observeOn(Schedulers.io())
                        .flatMap(integer -> characters.map(character -> character + integer.toString()), 4);
            } else {
                results = results
                        .observeOn(Schedulers.io())
                        .flatMap(string -> characters.map(character -> character + string), 4);
            }
        }
        results.blockingSubscribe(new PerformanceSubscriber(bh));
    }


    public static void main(String[] args) {
        //RxJavaFlatMap flatMapBenchmark = new RxJavaFlatMap();
        //flatMapBenchmark.singleFlatMap();
    }

}

