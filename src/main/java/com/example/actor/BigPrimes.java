package com.example.actor;

import akka.actor.typed.ActorSystem;

public class BigPrimes {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create(ManagerBehavior.create(), "big-primes");
        system.tell("start");
    }
}
