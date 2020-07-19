package pl.edu.agh.sukiennik.thesis.operators.transforming.scan;

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
public class ReactorScan {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    private Flux<Integer> singleScanFlux;
    private Flux<Integer> multiScanFlux;
    private Flux<Integer> multiScanEachOnIoFlux;

    @Setup
    public void setup() {
        singleScanFlux = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiScanFlux = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
        multiScanEachOnIoFlux = Flux.fromArray(IntStream.rangeClosed(0, times).boxed().toArray(Integer[]::new));
    }

    @TearDown(Level.Iteration)
    public void cleanup2() {
        ForcedGcMemoryProfiler.recordUsedMemory();
    }
    
    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleScan() {
        singleScanFlux
                .scan(0, Integer::sum)
                .then()
                .block();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiScan() {
        Flux<Integer> range = multiScanFlux;
        for (int i = 0; i < 10; i++) {
            range = range.scan(i, Integer::sum);
        }
        range.then().block();
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiScanEachOnIo() {
        Flux<Integer> range = multiScanEachOnIoFlux;
        for (int i = 0; i < 10; i++) {
            range = range.publishOn(Schedulers.elastic()).scan(i, Integer::sum);
        }
        range.then().block();
    }


    public static void main(String[] args) {
        //ReactorScan scanBenchmark = new ReactorScan();
        //scanBenchmark.multiScanEachOnIo();
    }

}



