package com.example;

import java.math.BigInteger;
import java.util.Random;

public class SortedPrimes {
    static BigInteger nextProbablePrime() {
        return new BigInteger(2000, new Random()).nextProbablePrime();
    }
}
