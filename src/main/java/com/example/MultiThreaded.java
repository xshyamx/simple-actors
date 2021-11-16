package com.example;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

public class MultiThreaded {
    static class Worker implements Runnable {

        Set<BigInteger> s;

        public Worker(Set<BigInteger> s) {
            this.s = s;
        }

        @Override
        public void run() {
            s.add(SortedPrimes.nextProbablePrime());
        }
    }
    static class CurrentProgress implements Runnable {
        Set<BigInteger> s;
        int n;

        public CurrentProgress(Set<BigInteger> s, int n) {
            this.s = s;
            this.n = n;
        }

        @Override
        public void run() {
            while ( s.size() < n ) {
                System.out.println("Size : " + s.size());
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();

        var s = new TreeSet<BigInteger>();
        var ts = new ArrayList<Thread>();
        final int n = 20;
        new Thread(new CurrentProgress(s, n)).start();
        for ( int i = 0; i < n; i++ ) {
            var t = new Thread(new Worker(s));
            ts.add(t);
            t.start();
        }
        for ( Thread t : ts ) {
            t.join();
        }
        long end = System.currentTimeMillis();
        System.out.printf("Generated %d numbers in %d ms\n%d\n%s", n, (end - start), s.size(), s);
    }
}
