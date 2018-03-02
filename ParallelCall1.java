package com.selva1;

import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ParallelCall1 {

    public static void main(String[] args) {
        List<Integer> listOfNumbers = new ArrayList<Integer>() {{
            add(1);
            add(2);
            add(3);
            add(4);
            add(5);
            add(6);
            add(7);
            add(8);
            add(9);
            add(10);
            add(11);
            add(12);
            add(13);
            add(14);
            add(15);
        }};

        final long from = System.currentTimeMillis();
        System.out.println("Begin Parallel call: " + from);
        List<Integer> listOfNumbers1 = new ArrayList<>();

        //Here we can specify the number of threads at a time can run. There are other Schedulers too.
        //Please refer the below link to have more details on schedulers.
        //https://proandroiddev.com/understanding-rxjava-subscribeon-and-observeon-744b0c6a41ea
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

        Observable
                .from(listOfNumbers)
                .flatMap(new Func1<Integer, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(Integer integer) {
                        Single<Integer> response = getNumberSingle(integer, executor)
                                .map(new Func1<Integer, Integer>() {
                                    @Override
                                    public Integer call(Integer integer) {
                                        return integer;
                                    }
                                });

                        return response.toObservable();
                    }
                })
                .toBlocking()
                .forEach(printResult(listOfNumbers1));
        //Whatever the response we returned in the "call" method will automatiaclly be passed to the above method to process it.

        System.out.println("Finished main: " + Thread.currentThread().getId());
        System.out.println("End of parallel call: " + (System.currentTimeMillis() - from));
        System.out.println(listOfNumbers1);
    }

    private static Single<Integer> getNumberSingle(Integer integer, ThreadPoolExecutor executor) {

        return Single.create(new Single.OnSubscribe<Integer>() {
            @Override
            public void call(SingleSubscriber<? super Integer> singleSubscriber) {
                try {
                    //Introduced the sleep time just to recognize whether its really running sequentially or parallel
                    //You can your own logic of what you need to do in parallel in this area.
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                singleSubscriber.onSuccess(integer); // Return your custom response here to process it further.
            }
        }).subscribeOn(Schedulers.from(executor));
    }

    private static Action1<Integer> printResult(List<Integer> listOfNumbers1) {

        return new Action1<Integer>() {
            @Override
            public void call(Integer result) {
                listOfNumbers1.add(result);
                System.out.println("Output: " + result + " Thread Name: " + Thread.currentThread().getName());
            }
        };
    }
}