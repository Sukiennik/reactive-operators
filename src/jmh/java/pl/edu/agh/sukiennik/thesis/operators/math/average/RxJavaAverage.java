package pl.edu.agh.sukiennik.thesis.operators.math.average;

import hu.akarnokd.rxjava3.math.MathFlowable;
import io.reactivex.rxjava3.core.Flowable;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import pl.edu.agh.sukiennik.thesis.operators.PerformanceSubscriber;

import java.util.concurrent.TimeUnit;
import java.util.stream.LongStream;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class RxJavaAverage {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flowable<Long> singleAverageFlowable;

    @Setup
    public void setup() {
        singleAverageFlowable = Flowable.fromArray(LongStream.rangeClosed(0, times).boxed().toArray(Long[]::new));
    }

    @Benchmark
    @Measurement(iterations = 5, time = 5)
    public void singleAverage(Blackhole bh) {
        MathFlowable.averageDouble(singleAverageFlowable).blockingSubscribe(new PerformanceSubscriber(bh));
    }

    public static void main(String[] args) {
        //RxJavaAverage averageBenchmark = new RxJavaAverage();
        //averageBenchmark.singleAverage();
    }

}



