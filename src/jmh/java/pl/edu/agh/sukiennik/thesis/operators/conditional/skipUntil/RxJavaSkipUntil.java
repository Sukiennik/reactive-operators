package pl.edu.agh.sukiennik.thesis.operators.conditional.skipUntil;

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
public class RxJavaSkipUntil {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flowable<Integer> singleSkipUntilFlowable;
    private Flowable<Integer> multiSkipUntilFlowable;
    private Flowable<Integer> multiSkipUntilEachOnIoFlowable;

    @Setup
    public void setup() {
        singleSkipUntilFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiSkipUntilFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiSkipUntilEachOnIoFlowable = Flowable.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 5)
    public void singleSkipUntil(Blackhole bh) {
        singleSkipUntilFlowable
                .skipWhile(value -> value <= times / 2)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 10)
    public void multiSkipUntil(Blackhole bh) {
        Flowable<Integer> range = multiSkipUntilFlowable;
        int condition = times;
        for (int i = 0; i < 10; i++) {
            condition = condition / 2;
            int finalCondition = condition;
            range = range.skipWhile(value -> value <= finalCondition);
        }
        range.blockingSubscribe(new PerformanceSubscriber(bh));
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiSkipUntilEachOnIo(Blackhole bh) {
        Flowable<Integer> range = multiSkipUntilEachOnIoFlowable;
        int condition = times;
        for (int i = 0; i < 10; i++) {
            condition = condition / 2;
            int finalCondition = condition;
            range = range.observeOn(Schedulers.io()).skipWhile(value -> value <= finalCondition);
        }
        range.blockingSubscribe(new PerformanceSubscriber(bh));
    }

    public static void main(String[] args) {
        //RxJavaSkipUntil firstBenchmark = new RxJavaSkipUntil();
        //firstBenchmark.singleSkipUntil();
    }

    /*
      no skipUntil -> using skipWhile
     */

}

