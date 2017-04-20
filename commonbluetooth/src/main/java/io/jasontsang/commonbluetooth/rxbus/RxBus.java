package io.jasontsang.commonbluetooth.rxbus;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by jason on 10/14/2015.
 */
public class RxBus {

    private final Subject<Object, Object> bus = new SerializedSubject<>(PublishSubject.create());

    public void send(Object o) {
        bus.onNext(o);
    }

    public Observable<Object> toObserverable() {
        return bus;
    }

    private ConcurrentHashMap<Object, List<Subject>> subjectMapper = new ConcurrentHashMap<>();

    public <T> Observable<T> register(@NonNull Object tag, @NonNull Class<T> c) {
        List<Subject> subjectList = subjectMapper.get(tag);
        if (null == subjectList) {
            subjectList = new ArrayList<>();
            subjectMapper.put(tag, subjectList);
            send(new RxBusEvent(tag, RxBusEvent.CREATE));
        }
        Subject<T, T> subject = PublishSubject.create();
        subjectList.add(subject);
        send(new RxBusEvent(tag, RxBusEvent.ADD, subject));
        return subject;
    }

    public void unregister(@NonNull Object tag, @NonNull Observable observable) {
        List<Subject> subjectList = subjectMapper.get(tag);
        if (null != subjectList) {
            subjectList.remove((Subject) observable);
            send(new RxBusEvent(tag, RxBusEvent.REMOVE, observable));
            if (subjectList.isEmpty()) {
                subjectMapper.remove(tag);
                send(new RxBusEvent(tag, RxBusEvent.DESTROY));
            }
        }
    }

    public void post(@NonNull Object tag, @NonNull Object content) {
        List<Subject> subjectList = subjectMapper.get(tag);
        if(null != subjectList) {
            for(Subject subject : subjectList) {
                subject.onNext(content);
            }
        }
    }

    public class RxBusEvent {
        public final static int CREATE = 0;
        public final static int ADD = 1;
        public final static int REMOVE = 2;
        public final static int DESTROY = 3;

        private Object tag;
        private int type;
        private Observable observable;

        private RxBusEvent() {}

        private RxBusEvent(@NonNull Object tag, int type) {
            this(tag, type, null);
        }

        private RxBusEvent(@NonNull Object tag, int type, Observable observable) {
            this.tag = tag;
            this.type = type;
            this.observable = observable;
        }

        public Object getTag() {
            return tag;
        }

        public void setTag(@NonNull Object tag) {
            this.tag = tag;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public Observable getObservable() {
            return observable;
        }

        public void setObservable(Observable observable) {
            this.observable = observable;
        }
    }
}