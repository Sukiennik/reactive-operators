package pl.edu.agh.sukiennik.thesis.operators.creating.defer;

import org.openjdk.jmh.annotations.*;
import reactor.core.publisher.Flux;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class ReactorDefer {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;
    
    @Benchmark
    @Measurement(iterations = 5, time = 1)
    public void singleDefer() {
        Flux.defer(() -> Flux.just(times))
                .then()
                .block();
    }

    public static void main(String[] args) {
        //ReactorDefer deferBenchmark = new ReactorDefer();
        //deferBenchmark.singleDefer();
    }

}

