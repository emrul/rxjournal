package org.rxjournal.examples.helloworld;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import org.rxjournal.impl.PlayOptions;
import org.rxjournal.impl.RxJournal;
import org.rxjournal.impl.RxPlayer;
import org.rxjournal.impl.RxRecorder;

import java.io.IOException;

/**
 * Simple Demo Program
 */
public class HelloWorld {
    public static void main(String[] args) throws IOException {
        //Create the rxRecorder and delete any previous content by clearing the cache
        RxJournal rxJournal = new RxJournal(System.getProperty("filename", "/tmp/Demo"));
        rxJournal.clearCache();

        Flowable<String> helloWorldFlowable = Flowable.just("Hello World!!");
        //Pass the flowable into the rxRecorder which will subscribe to it and record all events.
        RxRecorder rxRecorder = rxJournal.createRxRecorder();
        rxRecorder.record(helloWorldFlowable);

        RxPlayer rxPlayer = rxJournal.createRxPlayer();
        Observable recordedObservable = rxPlayer.play(new PlayOptions());

        recordedObservable.subscribe(System.out::println);

        //Sometimes useful to see the recording written to a file
        rxJournal.writeToFile("/tmp/Demo/demo.txt",true);
    }
}
