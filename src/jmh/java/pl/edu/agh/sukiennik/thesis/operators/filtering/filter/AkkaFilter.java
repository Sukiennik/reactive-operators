package pl.edu.agh.sukiennik.thesis.operators.filtering.filter;

import akka.NotUsed;
import akka.actor.ActorSystem;
import org.openjdk.jmh.annotations.*;
import akka.stream.javadsl.Source;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.SampleTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Thread)
public class AkkaFilter {

    @Param({"1", "1000", "1000000", "10000000"})
    private static int times;
    
    @State(Scope.Thread)
    public static class SingleFilterState {
        private Source<Integer, NotUsed> singleFilterSource;
        private ActorSystem singleFilterSystem;

        @Setup
        public void setup() {
            singleFilterSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            singleFilterSystem = ActorSystem.create("singleFilterSystem");
        }

        @TearDown
        public void cleanup() {
            singleFilterSystem.terminate();
        }
    }

    @State(Scope.Thread)
    public static class MultiFilterState {
        private Source<Integer, NotUsed> multiFilterSource;
        private ActorSystem multiFilterSystem;

        @Setup
        public void setup() {
            multiFilterSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            multiFilterSystem = ActorSystem.create("multiFilterSystem");
        }

        @TearDown
        public void cleanup() {
            multiFilterSystem.terminate();
        }
    }

    @State(Scope.Thread)
    public static class MultiFilterEachOnIoState {
        private Source<Integer, NotUsed> multiFilterEachOnIoSource;
        private ActorSystem multiFilterEachOnIoSystem;

        @Setup
        public void setup() {
            multiFilterEachOnIoSource = Source.fromJavaStream(() -> IntStream.rangeClosed(0, times));
            multiFilterEachOnIoSystem = ActorSystem.create("multiFilterEachOnIoSystem");
        }

        @TearDown
        public void cleanup() {
            multiFilterEachOnIoSystem.terminate();
        }
    }
    

    @Benchmark
    @Measurement(iterations = 5, time = 5)
    public void singleFilter(SingleFilterState state) throws ExecutionException, InterruptedException {
        state.singleFilterSource
                .filter(element -> element < times/2)
                .run(state.singleFilterSystem)
                .toCompletableFuture()
                .get();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 10)
    public void multiFilter(MultiFilterState state) throws ExecutionException, InterruptedException {
        Source<Integer, NotUsed> range = state.multiFilterSource;
        int condition = times;
        for (int i = 0; i < 10; i++) {
            condition = condition/2;
            int finalCondition = condition;
            range = range.filter(element -> element < finalCondition);
        }
        range.run(state.multiFilterSystem).toCompletableFuture().get();
    }

    @Benchmark
    @Measurement(iterations = 5, time = 20)
    public void multiFilterEachOnIo(MultiFilterEachOnIoState state) throws ExecutionException, InterruptedException {
        Source<Integer, NotUsed> range = state.multiFilterEachOnIoSource;
        int condition = times;
        for (int i = 0; i < 10; i++) {
            condition = condition/2;
            int finalCondition = condition;
            range = range.filter(element -> element < finalCondition).async();
        }
        range.run(state.multiFilterEachOnIoSystem).toCompletableFuture().get();
    }


    public static void main(String[] args) {
        //AkkaFilter filterBenchmark = new AkkaFilter();
        //filterBenchmark.singleFilter();
    }

}

