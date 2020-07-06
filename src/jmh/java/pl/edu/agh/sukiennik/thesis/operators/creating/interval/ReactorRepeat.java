package pl.edu.agh.sukiennik.thesis.operators.creating.interval;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class ReactorRepeat {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    @Benchmark
    @Measurement(iterations = 5, time = 1)
    public void singleRepeat() {
        Flux.interval(Duration.ZERO, Duration.ofMillis(1))
                .take(times)
                .then()
                .block();
    }

    public static void main(String[] args) {
        //ReactorRepeat repeatBenchmark = new ReactorRepeat();
        //repeatBenchmark.singleRepeat();
    }

}

