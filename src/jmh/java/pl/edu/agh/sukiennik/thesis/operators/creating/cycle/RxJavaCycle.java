package pl.edu.agh.sukiennik.thesis.operators.creating.cycle;

import io.reactivex.rxjava3.core.Flowable;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import pl.edu.agh.sukiennik.thesis.operators.PerformanceSubscriber;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class RxJavaCycle {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private List<Integer> cycleList;

    @Setup
    public void setup() {
        cycleList = IntStream.range(0, times).boxed().collect(Collectors.toList());
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleCycle(Blackhole bh) {
        Flowable.fromIterable(cycleList).repeat()
                .take(10 * times)
                .blockingSubscribe(new PerformanceSubscriber(bh));
    }

    public static void main(String[] args) {
        //RxJavaCycle cycleBenchmark = new RxJavaCycle();
        //cycleBenchmark.singleCycle();
    }

}

