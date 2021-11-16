package com.example;

import java.math.BigInteger;
import java.util.TreeSet;

public class SingleThreaded {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        var s = new TreeSet<BigInteger>();
        final int n = 20;
        for ( int i = 0; i < n; i++ ) {
            s.add(SortedPrimes.nextProbablePrime());
        }
        long end = System.currentTimeMillis();
        System.out.printf("Generated %d numbers in %d ms\n%d\n%s", n, (end - start), s.size(), s);
    }
}
