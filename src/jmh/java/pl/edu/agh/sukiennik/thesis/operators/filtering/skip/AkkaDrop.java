package pl.edu.agh.sukiennik.thesis.operators.filtering.skip;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;
import pl.edu.agh.sukiennik.thesis.operators.ForcedGcMemoryProfiler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class AkkaDrop {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    @State(Scope.Thread)
    public static class SingleSkipState {
        private Source<Integer, NotUsed> singleSkip;
        private ActorSystem singleSkipSystem;

        @Setup
        public void setup() {
            singleSkip = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            singleSkipSystem = ActorSystem.create("singleSkipSystem");
        }

        @TearDown
        public void cleanup() {
            singleSkipSystem.terminate();
        }

        @TearDown(Level.Iteration)
        public void cleanup2() {
            ForcedGcMemoryProfiler.recordUsedMemory();
        }
    }

    @State(Scope.Thread)
    public static class MultiSkipState {
        private Source<Integer, NotUsed> multiSkipSource;
        private ActorSystem multiSkipSystem;

        @Setup
        public void setup() {
            multiSkipSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            multiSkipSystem = ActorSystem.create("multiSkipSystem");
        }

        @TearDown
        public void cleanup() {
            multiSkipSystem.terminate();
        }

        @TearDown(Level.Iteration)
        public void cleanup2() {
            ForcedGcMemoryProfiler.recordUsedMemory();
        }
    }

    @State(Scope.Thread)
    public static class MultiSkipEachOnIoState {
        private Source<Integer, NotUsed> multiSkipEachOnIoSource;
        private ActorSystem multiSkipEachOnIoSystem;

        @Setup
        public void setup() {
            multiSkipEachOnIoSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            multiSkipEachOnIoSystem = ActorSystem.create("multiSkipEachOnIoSystem");
        }

        @TearDown
        public void cleanup() {
            multiSkipEachOnIoSystem.terminate();
        }

        @TearDown(Level.Iteration)
        public void cleanup2() {
            ForcedGcMemoryProfiler.recordUsedMemory();
        }
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleSkip(SingleSkipState state) throws ExecutionException, InterruptedException {
        state.singleSkip
                .drop(times / 2)
                .run(state.singleSkipSystem)
                .toCompletableFuture()
                .get();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiSkip(MultiSkipState state) throws ExecutionException, InterruptedException {
        Source<Integer, NotUsed> range = state.multiSkipSource;
        int elements = times;
        for (int i = 0; i < 10; i++) {
            elements = elements / 2;
            int dropCount = elements;
            range = range.drop(dropCount);
        }
        range.run(state.multiSkipSystem).toCompletableFuture().get();
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiSkipEachOnIo(MultiSkipEachOnIoState state) throws ExecutionException, InterruptedException {
        Source<Integer, NotUsed> range = state.multiSkipEachOnIoSource;
        int elements = times;
        for (int i = 0; i < 10; i++) {
            elements = elements / 2;
            int dropCount = elements;
            range = range.drop(dropCount).async();
        }
        range.run(state.multiSkipEachOnIoSystem).toCompletableFuture().get();
    }

    public static void main(String[] args) {
        //AkkaSkip skipBenchmark = new AkkaSkip();
        //skipBenchmark.singleSkip();
    }
}

