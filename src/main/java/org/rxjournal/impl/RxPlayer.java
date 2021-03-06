package org.rxjournal.impl;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import net.openhft.chronicle.queue.ChronicleQueue;
import net.openhft.chronicle.queue.ExcerptTailer;
import net.openhft.chronicle.wire.ValueIn;
import org.rxjournal.util.DSUtil;

/**
 * Class to playback data recorded into RxJournal.
 */
public class RxPlayer {
    private RxJournal rxJournal;
    private DataItemProcessor dim = new DataItemProcessor();

    RxPlayer(RxJournal rxJournal) {
        this.rxJournal = rxJournal;
    }

    public Observable play(PlayOptions options) {
        long fromTime = System.currentTimeMillis();

        return Observable.create(subscriber -> {
            try (ChronicleQueue queue = rxJournal.createQueue()) {
                ExcerptTailer tailer = queue.createTailer();
                long[] lastTime = new long[]{Long.MIN_VALUE};
                boolean[] stop = new boolean[]{false};
                while (true) {

                    boolean foundItem = tailer.readDocument(w -> {
                        ValueIn in = w.getValueIn();
                        dim.process(in, null);

                        if (testPastPlayUntil(options, subscriber, dim.getTime())){
                            stop[0] = true;
                            return;
                        }

                        if (options.playFrom() > dim.getTime()
                                && (!options.playFromNow() || fromTime < dim.getTime())) {
                            pause(options, lastTime, dim.getTime());

                            if (options.filter().equals(dim.getFilter())) {
                                if (dim.getStatus()==RxStatus.COMPLETE) {
                                    subscriber.onComplete();
                                    stop[0] = true;
                                    return;
                                }

                                if (dim.getStatus()==RxStatus.ERROR){
                                    subscriber.onError((Throwable)dim.getObject());
                                    stop[0] = true;
                                    return;
                                }
                                subscriber.onNext(dim.getObject());
                            }
                            lastTime[0] = dim.getTime();
                        }
                    });
                    if (!foundItem && !options.completeAtEndOfFile() || stop[0]) {
                        subscriber.onComplete();
                        return;
                    }
                }
            }

        });
    }

    private boolean testPastPlayUntil(PlayOptions options, Emitter<? super Object> s, long recordedAtTime) {
        if(options.playUntil() > recordedAtTime){
            s.onComplete();
            return true;
        }
        return false;
    }

    private void pause(PlayOptions options, long[] lastTime, long recordedAtTime) {
        if (options.replayStrategy() == PlayOptions.Replay.REAL_TIME && lastTime[0] != Long.MIN_VALUE) {
            DSUtil.sleep((int) (recordedAtTime - lastTime[0]));
        }
        //todo add configurable pause strategy
    }
}
