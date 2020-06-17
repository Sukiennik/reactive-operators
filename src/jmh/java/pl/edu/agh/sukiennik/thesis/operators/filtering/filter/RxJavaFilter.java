package pl.edu.agh.sukiennik.thesis.operators.filtering.filter;

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
public class RxJavaFilter {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flowable<Integer> singleFilterFlowable;
    private Flowable<Integer> multiFilterFlowable;
    private Flowable<Integer> multiFilterEachOnIoFlowable;

    @Setup
    public void setup() {
        singleFilterFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiFilterFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiFilterEachOnIoFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 5)
    public void singleFilter() {
        singleFilterFlowable
                .filter(element -> element < times/2)
                .blockingSubscribe();
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 10)
    public void multiFilter(Blackhole bh) {
        Flowable<Integer> range = multiFilterFlowable;
        int condition = times;
        for (int i = 0; i < 10; i++) {
            condition = condition/2;
            int finalCondition = condition;
            range = range.filter(element -> element < finalCondition);
        }
        range.blockingSubscribe(new PerformanceSubscriber(bh));
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiFilterEachOnIo(Blackhole bh) {
        Flowable<Integer> range = multiFilterEachOnIoFlowable;
        int condition = times;
        for (int i = 0; i < 10; i++) {
            condition = condition/2;
            int finalCondition = condition;
            range = range.observeOn(Schedulers.io()).filter(element -> element < finalCondition);
        }
        range.blockingSubscribe(new PerformanceSubscriber(bh));
    }


    public static void main(String[] args) {
        //RxJavaFilter filterBenchmark = new RxJavaFilter();
        //filterBenchmark.singleFilter();
    }

}

