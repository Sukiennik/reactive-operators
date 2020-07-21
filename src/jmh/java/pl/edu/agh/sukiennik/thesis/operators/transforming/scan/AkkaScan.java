package pl.edu.agh.sukiennik.thesis.operators.transforming.scan;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.utils.ForcedGcMemoryProfiler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class AkkaScan {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    @State(Scope.Thread)
    public static class SingleScanState {
        private Source<Integer, NotUsed> singleScanSource;
        private ActorSystem singleScanSystem;

        @Setup
        public void setup() {
            singleScanSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            singleScanSystem = ActorSystem.create("singleScanSystem");
        }

        @TearDown
        public void cleanup() {
            singleScanSystem.terminate();
        }

        @TearDown(Level.Iteration)
        public void cleanup2() {
            ForcedGcMemoryProfiler.recordUsedMemory();
        }
    }

    @State(Scope.Thread)
    public static class MultiScanState {
        private Source<Integer, NotUsed> multiScanSource;
        private ActorSystem multiScanSystem;

        @Setup
        public void setup() {
            multiScanSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            multiScanSystem = ActorSystem.create("multiScanSystem");
        }

        @TearDown
        public void cleanup() {
            multiScanSystem.terminate();
        }

        @TearDown(Level.Iteration)
        public void cleanup2() {
            ForcedGcMemoryProfiler.recordUsedMemory();
        }
    }

    @State(Scope.Thread)
    public static class MultiScanEachOnIoState {
        private Source<Integer, NotUsed> multiScanEachOnIoSource;
        private ActorSystem multiScanEachOnIoSystem;

        @Setup
        public void setup() {
            multiScanEachOnIoSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            multiScanEachOnIoSystem = ActorSystem.create("multiScanEachOnIoSystem");
        }

        @TearDown
        public void cleanup() {
            multiScanEachOnIoSystem.terminate();
        }

        @TearDown(Level.Iteration)
        public void cleanup2() {
            ForcedGcMemoryProfiler.recordUsedMemory();
        }
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleScan(SingleScanState state) throws ExecutionException, InterruptedException {
        state.singleScanSource
                .scan(0, Integer::sum)
                .run(state.singleScanSystem)
                .toCompletableFuture()
                .get();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiScan(MultiScanState state) throws ExecutionException, InterruptedException {
        Source<Integer, NotUsed> range = state.multiScanSource;
        for (int i = 0; i < 10; i++) {
            range = range.scan(i, Integer::sum);
        }
        range.run(state.multiScanSystem).toCompletableFuture().get();
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiScanEachOnIo(MultiScanEachOnIoState state) throws ExecutionException, InterruptedException {
        Source<Integer, NotUsed> range = state.multiScanEachOnIoSource;
        for (int i = 0; i < 10; i++) {
            range = range.scan(i, Integer::sum).async();
        }
        range.run(state.multiScanEachOnIoSystem).toCompletableFuture().get();
    }

    public static void main(String[] args) {
        //AkkaScan scanBenchmark = new AkkaScan();
        //scanBenchmark.multiScanEachOnIo();
    }
}



