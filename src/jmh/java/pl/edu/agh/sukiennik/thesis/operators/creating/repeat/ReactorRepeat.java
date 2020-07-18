package pl.edu.agh.sukiennik.thesis.operators.creating.repeat;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import reactor.core.publisher.Flux;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class ReactorRepeat {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<Integer> singleRepeat;

    @Setup
    public void setup() {
        singleRepeat = Flux.just(1);
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleRepeat() {
        singleRepeat
                .repeat(times)
                .then()
                .block();
    }

    public static void main(String[] args) {
        //ReactorRepeat repeatBenchmark = new ReactorRepeat();
        //repeatBenchmark.singleRepeat();
    }

}

