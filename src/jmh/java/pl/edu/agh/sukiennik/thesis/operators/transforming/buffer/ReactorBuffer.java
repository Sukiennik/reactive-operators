package pl.edu.agh.sukiennik.thesis.operators.transforming.buffer;

import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.operators.ForcedGcMemoryProfiler;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class ReactorBuffer {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<Integer> singleBufferFlux;
    private Flux<Integer> multiBufferFlux;
    private Flux<Integer> multiBufferEachOnIoFlux;

    @Setup
    public void setup() {
        singleBufferFlux = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiBufferFlux = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiBufferEachOnIoFlux = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleBuffer() {
        singleBufferFlux
                .buffer(5)
                .then()
                .block();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiBuffer() {
        multiBufferFlux
                .buffer(5)
                .buffer(5)
                .buffer(5)
                .buffer(5)
                .buffer(5)
                .buffer(5)
                .buffer(5)
                .buffer(5)
                .buffer(5)
                .buffer(5)
                .then().block();
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiBufferEachOnIo() {
        multiBufferEachOnIoFlux
                .publishOn(Schedulers.elastic()).buffer(5)
                .publishOn(Schedulers.elastic()).buffer(5)
                .publishOn(Schedulers.elastic()).buffer(5)
                .publishOn(Schedulers.elastic()).buffer(5)
                .publishOn(Schedulers.elastic()).buffer(5)
                .publishOn(Schedulers.elastic()).buffer(5)
                .publishOn(Schedulers.elastic()).buffer(5)
                .publishOn(Schedulers.elastic()).buffer(5)
                .publishOn(Schedulers.elastic()).buffer(5)
                .publishOn(Schedulers.elastic()).buffer(5)
                .then().block();
    }


    public static void main(String[] args) {
        //ReactorBuffer bufferBenchmark = new ReactorBuffer();
        //bufferBenchmark.multiBufferEachOnIo();
    }

}



