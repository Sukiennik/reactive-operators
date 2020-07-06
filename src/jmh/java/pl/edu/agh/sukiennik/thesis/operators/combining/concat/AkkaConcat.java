package pl.edu.agh.sukiennik.thesis.operators.combining.concat;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.javadsl.Source;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class AkkaConcat {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;
    
    @State(Scope.Thread)
    public static class SingleConcatState {
        private Source<String, NotUsed> singleConcatSource;
        private Source<String, NotUsed> concatSource;
        private ActorSystem singleConcatSystem;

        @Setup
        public void setup() {
            singleConcatSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times).mapToObj(String::valueOf));
            concatSource = Source.fromJavaStream(() -> IntStream.rangeClosed(times, times * 2 / 3).mapToObj(String::valueOf));
            singleConcatSystem = ActorSystem.create("singleConcatSystem");
        }

        @TearDown
        public void cleanup() {
            singleConcatSystem.terminate();
        }
    }

    @State(Scope.Thread)
    public static class MultiConcatState {
        private Source<String, NotUsed> multiConcatSource;
        private Source<String, NotUsed> concatSource;
        private ActorSystem multiConcatSystem;

        @Setup
        public void setup() {
            multiConcatSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times).mapToObj(String::valueOf));
            concatSource = Source.fromJavaStream(() -> IntStream.rangeClosed(times, times * 2 / 3).mapToObj(String::valueOf));
            multiConcatSystem = ActorSystem.create("multiConcatSystem");
        }

        @TearDown
        public void cleanup() {
            multiConcatSystem.terminate();
        }
    }

    @State(Scope.Thread)
    public static class MultiConcatEachOnIoState {
        private Source<String, NotUsed> multiConcatEachOnIoSource;
        private Source<String, NotUsed> concatSource;
        private ActorSystem multiConcatEachOnIoSystem;

        @Setup
        public void setup() {
            multiConcatEachOnIoSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times).mapToObj(String::valueOf));
            concatSource = Source.fromJavaStream(() -> IntStream.rangeClosed(times, times * 2 / 3).mapToObj(String::valueOf));
            multiConcatEachOnIoSystem = ActorSystem.create("multiConcatEachOnIoSystem");
        }

        @TearDown
        public void cleanup() {
            multiConcatEachOnIoSystem.terminate();
        }
    }
    

    @Benchmark
    @Measurement(iterations = 5, time = 5)
    public void singleConcat(SingleConcatState state) throws ExecutionException, InterruptedException {
        state.singleConcatSource
                .concat(state.concatSource)
                .run(state.singleConcatSystem)
                .toCompletableFuture()
                .get();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 10)
    public void multiConcat(MultiConcatState state) throws ExecutionException, InterruptedException {
        Source<String, NotUsed> range = state.multiConcatSource;
        for (int i = 0; i < 10; i++) {
            range = range.concat(state.concatSource);
        }
        range.run(state.multiConcatSystem).toCompletableFuture().get();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiConcatEachOnIo(MultiConcatEachOnIoState state) throws ExecutionException, InterruptedException {
        Source<String, NotUsed> range = state.multiConcatEachOnIoSource;
        for (int i = 0; i < 10; i++) {
            range = range.concat(state.concatSource).async();
        }
        range.run(state.multiConcatEachOnIoSystem).toCompletableFuture().get();
    }


    public static void main(String[] args) {
        //AkkaConcat concatBenchmark = new AkkaConcat();
        //concatBenchmark.singleConcat();
    }

}

