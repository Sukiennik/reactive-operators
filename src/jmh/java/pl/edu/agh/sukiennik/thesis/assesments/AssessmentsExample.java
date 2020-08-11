package pl.edu.agh.sukiennik.thesis.assesments;

import akka.NotUsed;
import akka.japi.Pair;
import akka.stream.javadsl.Source;
import io.reactivex.rxjava3.core.Flowable;
import pl.edu.agh.sukiennik.thesis.assesments.model.Book;
import reactor.core.publisher.Flux;

import java.util.Optional;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class AssessmentsExample {

    public Flux<String> taskZip(Flux<String> sequenceA, Flux<String> sequenceB) {
        return Flux.zip(sequenceA, sequenceB, (a, b) -> a.concat(" ").concat(b));
    }

    public Source<Integer, NotUsed> taskFibonacci(int n) {
        return Source.unfold(
                Pair.create(0, 1),
                current -> {
                    int a = current.first();
                    int b = current.second();
                    int next = a + b;
                    if (next > n) return Optional.empty();
                    Pair<Integer, Integer> nextPair = Pair.create(b, next);
                    return Optional.of(Pair.create(nextPair, a));
                });
    }

    public Source<Integer, NotUsed> taskFactorials(int n) {
        return Source.range(2, n).scan(1, (acc, next) -> acc * next);
    }

    public Flowable<String> taskEvenWithSuffix(Flowable<Integer> sequence) {
        return sequence.filter(number -> number % 2 == 0).map(evenNumber -> evenNumber + "_even");
    }

    public Flowable<String> taskSmallestSize(Flowable<Book> books) {
        return books.flatMap(book -> Flowable.fromIterable(book.chapters).flatMap(chapter ->
                Flowable.fromIterable(chapter.images).reduce((imageOne, imageTwo) -> {
                    int imgOne = imageOne.height & imageOne.width;
                    int imgTwo = imageOne.height & imageOne.width;
                    return imgOne < imgTwo ? imageOne : imageTwo;
                }).toFlowable().map(image -> book.toString() + chapter.toString() + image.toString())
        ));
    }

    public Flowable<String> taskTellWords(String sentence, long timeForLetter) {
        String[] speakableWords = sentence.replaceAll("[:,]", "").split(" ");
        Flowable<String> wordsSequence = Flowable.fromArray(speakableWords);
        Flowable<Long> wordSpeakTimeSequence = wordsSequence
                .map(String::length)
                .map(len -> len * timeForLetter)
                .scan(Long::sum);
        return wordsSequence
                .zipWith(wordSpeakTimeSequence.startWithItem(0L), Pair::new)
                .flatMap(pair -> Flowable
                        .just(pair.first())
                        .delay(pair.second(), MILLISECONDS)
                );
    }
}
