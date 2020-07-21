package pl.edu.agh.sukiennik.thesis.operators.combining.combineLatest;

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
public class AkkaCombineLatest {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;
    
    @State(Scope.Thread)
    public static class SingleCombineLatestState {
        private Source<String, NotUsed> singleCombineLatestSource;
        private Source<String, NotUsed> combineLatestSource;
        private ActorSystem singleCombineLatestSystem;

        @Setup(Level.Iteration)
        public void setup() {
            singleCombineLatestSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times).mapToObj(String::valueOf));
            combineLatestSource = Source.fromJavaStream(() -> IntStream.rangeClosed(times, times * 2 / 3).mapToObj(String::valueOf));
            singleCombineLatestSystem = ActorSystem.create("singleCombineLatestSystem");
        }

        @TearDown(Level.Iteration)
        public void cleanup() {
            ForcedGcMemoryProfiler.recordUsedMemory();
            singleCombineLatestSystem.terminate();
        }
    }

    @State(Scope.Thread)
    public static class MultiCombineLatestState {
        private Source<String, NotUsed> multiCombineLatestSource;
        private Source<String, NotUsed> combineLatestSource;
        private ActorSystem multiCombineLatestSystem;

        @Setup(Level.Iteration)
        public void setup() {
            multiCombineLatestSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times).mapToObj(String::valueOf));
            combineLatestSource = Source.fromJavaStream(() -> IntStream.rangeClosed(times, times * 2 / 3).mapToObj(String::valueOf));
            multiCombineLatestSystem = ActorSystem.create("multiCombineLatestSystem");
        }

        @TearDown(Level.Iteration)
        public void cleanup() {
            multiCombineLatestSystem.terminate();
        }
    }

    @State(Scope.Thread)
    public static class MultiCombineLatestEachOnIoState {
        private Source<String, NotUsed> multiCombineLatestEachOnIoSource;
        private Source<String, NotUsed> combineLatestSource;
        private ActorSystem multiCombineLatestEachOnIoSystem;

        @Setup(Level.Iteration)
        public void setup() {
            multiCombineLatestEachOnIoSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times).mapToObj(String::valueOf));
            combineLatestSource = Source.fromJavaStream(() -> IntStream.rangeClosed(times, times * 2 / 3).mapToObj(String::valueOf));
            multiCombineLatestEachOnIoSystem = ActorSystem.create("multiCombineLatestEachOnIoSystem");
        }

        @TearDown(Level.Iteration)
        public void cleanup() {
            multiCombineLatestEachOnIoSystem.terminate();
        }
    }
    

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleCombineLatest(SingleCombineLatestState state) throws ExecutionException, InterruptedException {
        state.singleCombineLatestSource
                .zipLatestWith(state.combineLatestSource, String::concat)
                .run(state.singleCombineLatestSystem)
                .toCompletableFuture()
                .get();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiCombineLatest(MultiCombineLatestState state) throws ExecutionException, InterruptedException {
        Source<String, NotUsed> range = state.multiCombineLatestSource;
        for (int i = 0; i < 10; i++) {
            range = range.zipLatestWith(state.combineLatestSource, String::concat);
        }
        range.run(state.multiCombineLatestSystem).toCompletableFuture().get();
    }

    //@Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiCombineLatestEachOnIo(MultiCombineLatestEachOnIoState state) throws ExecutionException, InterruptedException {
        Source<String, NotUsed> range = state.multiCombineLatestEachOnIoSource;
        for (int i = 0; i < 10; i++) {
            range = range.zipLatestWith(state.combineLatestSource, String::concat).async();
        }
        range.run(state.multiCombineLatestEachOnIoSystem).toCompletableFuture().get();
    }


    public static void main(String[] args) {
        //AkkaCombineLatest combineLatestBenchmark = new AkkaCombineLatest();
        //combineLatestBenchmark.singleCombineLatest();
    }

}

