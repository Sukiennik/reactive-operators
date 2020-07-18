package pl.edu.agh.sukiennik.thesis.operators.combining.zip;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 5)
@Fork(1)
@State(Scope.Thread)
public class AkkaZip {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;
    
    @State(Scope.Thread)
    public static class SingleZipState {
        private Source<String, NotUsed> singleZipSource;
        private Source<String, NotUsed> zipSource;
        private ActorSystem singleZipSystem;

        @Setup(Level.Iteration)
        public void setup() {
            singleZipSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times).mapToObj(String::valueOf));
            zipSource = Source.fromJavaStream(() -> IntStream.rangeClosed(times, times * 2 / 3).mapToObj(String::valueOf));
            singleZipSystem = ActorSystem.create("singleZipSystem");
        }

        @TearDown(Level.Iteration)
        public void cleanup() {
            singleZipSystem.terminate();
        }
    }

    @State(Scope.Thread)
    public static class MultiZipState {
        private Source<String, NotUsed> multiZipSource;
        private Source<String, NotUsed> zipSource;
        private ActorSystem multiZipSystem;

        @Setup(Level.Iteration)
        public void setup() {
            multiZipSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times).mapToObj(String::valueOf));
            zipSource = Source.fromJavaStream(() -> IntStream.rangeClosed(times, times * 2 / 3).mapToObj(String::valueOf));
            multiZipSystem = ActorSystem.create("multiZipSystem");
        }

        @TearDown(Level.Iteration)
        public void cleanup() {
            multiZipSystem.terminate();
        }
    }

    @State(Scope.Thread)
    public static class MultiZipEachOnIoState {
        private Source<String, NotUsed> multiZipEachOnIoSource;
        private Source<String, NotUsed> zipSource;
        private ActorSystem multiZipEachOnIoSystem;

        @Setup(Level.Iteration)
        public void setup() {
            multiZipEachOnIoSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times).mapToObj(String::valueOf));
            zipSource = Source.fromJavaStream(() -> IntStream.rangeClosed(times, times * 2 / 3).mapToObj(String::valueOf));
            multiZipEachOnIoSystem = ActorSystem.create("multiZipEachOnIoSystem");
        }

        @TearDown(Level.Iteration)
        public void cleanup() {
            multiZipEachOnIoSystem.terminate();
        }
    }
    

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void singleZip(SingleZipState state) throws ExecutionException, InterruptedException {
        state.singleZipSource
                .zipWith(state.zipSource, String::concat)
                .run(state.singleZipSystem)
                .toCompletableFuture()
                .get();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiZip(MultiZipState state) throws ExecutionException, InterruptedException {
        Source<String, NotUsed> range = state.multiZipSource;
        for (int i = 0; i < 10; i++) {
            range = range.zipWith(state.zipSource, String::concat);
        }
        range.run(state.multiZipSystem).toCompletableFuture().get();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiZipEachOnIo(MultiZipEachOnIoState state) throws ExecutionException, InterruptedException {
        Source<String, NotUsed> range = state.multiZipEachOnIoSource;
        for (int i = 0; i < 10; i++) {
            range = range.zipWith(state.zipSource, String::concat).async();
        }
        range.run(state.multiZipEachOnIoSystem).toCompletableFuture().get();
    }


    public static void main(String[] args) {
        //AkkaZip zipBenchmark = new AkkaZip();
        //zipBenchmark.singleZip();
    }

}

