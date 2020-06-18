package pl.edu.agh.sukiennik.thesis.operators.transofrming.map;

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
public class RxJavaMap {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flowable<Integer> singleMapFlowable;
    private Flowable<Integer> multiMapFlowable;
    private Flowable<Integer> multiMapEachOnIoFlowable;

    @Setup
    public void setup() {
        singleMapFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiMapFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiMapEachOnIoFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }
    
    @Benchmark
    @Measurement(iterations = 5, time = 5)
    public void singleMap(Blackhole bh) {
        singleMapFlowable
                .map(element -> element + 1)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 10)
    public void multiMap(Blackhole bh) {
        Flowable<Integer> range = multiMapFlowable;
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            range = range.map(element -> element + finalI);
        }
        range.blockingSubscribe(new PerformanceSubscriber(bh));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiMapEachOnIo(Blackhole bh) {
        Flowable<Integer> range = multiMapEachOnIoFlowable;
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            range = range.observeOn(Schedulers.io()).map(element -> element + finalI);
        }
        range.blockingSubscribe(new PerformanceSubscriber(bh));
    }


    public static void main(String[] args) {
        //RxJavaMap mapBenchmark = new RxJavaMap();
        //mapBenchmark.singleMap();
    }

}

