package pl.edu.agh.sukiennik.thesis.operators.combining.merge;

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
public class AkkaMerge {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;
    
    @State(Scope.Thread)
    public static class SingleMergeState {
        private Source<String, NotUsed> singleMergeSource;
        private Source<String, NotUsed> mergedSource;
        private ActorSystem singleMergeSystem;

        @Setup(Level.Iteration)
        public void setup() {
            singleMergeSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times).mapToObj(String::valueOf));
            mergedSource = Source.fromJavaStream(() -> IntStream.rangeClosed(times, times * 2 / 3).mapToObj(String::valueOf));
            singleMergeSystem = ActorSystem.create("singleMergeSystem");
        }

        @TearDown(Level.Iteration)
        public void cleanup() {
            ForcedGcMemoryProfiler.recordUsedMemory();
            singleMergeSystem.terminate();
        }
    }

    @State(Scope.Thread)
    public static class MultiMergeState {
        private Source<String, NotUsed> multiMergeSource;
        private Source<String, NotUsed> mergedSource;
        private ActorSystem multiMergeSystem;

        @Setup(Level.Iteration)
        public void setup() {
            multiMergeSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times).mapToObj(String::valueOf));
            mergedSource = Source.fromJavaStream(() -> IntStream.rangeClosed(times, times * 2 / 3).mapToObj(String::valueOf));
            multiMergeSystem = ActorSystem.create("multiMergeSystem");
        }

        @TearDown(Level.Iteration)
        public void cleanup() {
            multiMergeSystem.terminate();
        }
    }

    @State(Scope.Thread)
    public static class MultiMergeEachOnIoState {
        private Source<String, NotUsed> multiMergeEachOnIoSource;
        private Source<String, NotUsed> mergedSource;
        private ActorSystem multiMergeEachOnIoSystem;

        @Setup(Level.Iteration)
        public void setup() {
            multiMergeEachOnIoSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times).mapToObj(String::valueOf));
            mergedSource = Source.fromJavaStream(() -> IntStream.rangeClosed(times, times * 2 / 3).mapToObj(String::valueOf));
            multiMergeEachOnIoSystem = ActorSystem.create("multiMergeEachOnIoSystem");
        }

        @TearDown(Level.Iteration)
        public void cleanup() {
            multiMergeEachOnIoSystem.terminate();
        }
    }
    

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleMerge(SingleMergeState state) throws ExecutionException, InterruptedException {
        state.singleMergeSource
                .merge(state.mergedSource)
                .run(state.singleMergeSystem)
                .toCompletableFuture()
                .get();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiMerge(MultiMergeState state) throws ExecutionException, InterruptedException {
        Source<String, NotUsed> range = state.multiMergeSource;
        for (int i = 0; i < 10; i++) {
            range = range.merge(state.mergedSource);
        }
        range.run(state.multiMergeSystem).toCompletableFuture().get();
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiMergeEachOnIo(MultiMergeEachOnIoState state) throws ExecutionException, InterruptedException {
        Source<String, NotUsed> range = state.multiMergeEachOnIoSource;
        for (int i = 0; i < 10; i++) {
            range = range.merge(state.mergedSource).async();
        }
        range.run(state.multiMergeEachOnIoSystem).toCompletableFuture().get();
    }


    public static void main(String[] args) {
        //AkkaMerge mergeBenchmark = new AkkaMerge();
        //mergeBenchmark.singleMerge();
    }

}

