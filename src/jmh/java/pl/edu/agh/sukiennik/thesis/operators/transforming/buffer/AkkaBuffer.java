package pl.edu.agh.sukiennik.thesis.operators.transforming.buffer;

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
public class AkkaBuffer {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;

    @State(Scope.Thread)
    public static class SingleBufferState {
        private Source<Integer, NotUsed> singleBufferSource;
        private ActorSystem singleBufferSystem;

        @Setup
        public void setup() {
            singleBufferSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            singleBufferSystem = ActorSystem.create("singleBufferSystem");
        }

        @TearDown
        public void cleanup() {
            singleBufferSystem.terminate();
        }

        @TearDown(Level.Iteration)
        public void cleanup2() {
            ForcedGcMemoryProfiler.recordUsedMemory();
        }
    }

    @State(Scope.Thread)
    public static class MultiBufferState {
        private Source<Integer, NotUsed> multiBufferSource;
        private ActorSystem multiBufferSystem;

        @Setup
        public void setup() {
            multiBufferSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            multiBufferSystem = ActorSystem.create("multiBufferSystem");
        }

        @TearDown
        public void cleanup() {
            multiBufferSystem.terminate();
        }

        @TearDown(Level.Iteration)
        public void cleanup2() {
            ForcedGcMemoryProfiler.recordUsedMemory();
        }
    }

    @State(Scope.Thread)
    public static class MultiBufferEachOnIoState {
        private Source<Integer, NotUsed> multiBufferEachOnIoSource;
        private ActorSystem multiBufferEachOnIoSystem;

        @Setup
        public void setup() {
            multiBufferEachOnIoSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            multiBufferEachOnIoSystem = ActorSystem.create("multiBufferEachOnIoSystem");
        }

        @TearDown
        public void cleanup() {
            multiBufferEachOnIoSystem.terminate();
        }

        @TearDown(Level.Iteration)
        public void cleanup2() {
            ForcedGcMemoryProfiler.recordUsedMemory();
        }
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleBuffer(SingleBufferState state) throws ExecutionException, InterruptedException {
        state.singleBufferSource
                .grouped(5)
                .run(state.singleBufferSystem)
                .toCompletableFuture()
                .get();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiBuffer(MultiBufferState state) throws ExecutionException, InterruptedException {
        state.multiBufferSource
                .grouped(5)
                .grouped(5)
                .grouped(5)
                .grouped(5)
                .grouped(5)
                .grouped(5)
                .grouped(5)
                .grouped(5)
                .grouped(5)
                .grouped(5)
                .run(state.multiBufferSystem).toCompletableFuture().get();
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiBufferEachOnIo(MultiBufferEachOnIoState state) throws ExecutionException, InterruptedException {
        state.multiBufferEachOnIoSource
                .grouped(5).async()
                .grouped(5).async()
                .grouped(5).async()
                .grouped(5).async()
                .grouped(5).async()
                .grouped(5).async()
                .grouped(5).async()
                .grouped(5).async()
                .grouped(5).async()
                .grouped(5).async()
                .run(state.multiBufferEachOnIoSystem).toCompletableFuture().get();
    }

    public static void main(String[] args) {
        //AkkaBuffer bufferBenchmark = new AkkaBuffer();
        //bufferBenchmark.multiBufferEachOnIo();
    }
}



