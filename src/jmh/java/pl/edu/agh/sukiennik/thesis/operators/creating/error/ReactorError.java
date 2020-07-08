package pl.edu.agh.sukiennik.thesis.operators.creating.error;

import org.openjdk.jmh.annotations.*;
import reactor.core.publisher.Flux;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class ReactorError {
    
    @Benchmark
    @Measurement(iterations = 5, time = 1)
    public void singleError() {
        Flux.error(Exception::new)
                .then()
                .block();
    }

    public static void main(String[] args) {
        //ReactorError errorBenchmark = new ReactorError();
        //errorBenchmark.singleError();
    }

}

