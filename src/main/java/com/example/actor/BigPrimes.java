package com.example.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;

import java.math.BigInteger;
import java.time.Duration;
import java.util.SortedSet;
import java.util.concurrent.CompletionStage;

public class BigPrimes {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create(ManagerBehavior.create(), "big-primes");
//        system.tell(new ManagerBehavior.InstructionCommand("start"));
        CompletionStage<SortedSet<BigInteger>> result = AskPattern.ask(
                system,
                (ActorRef<SortedSet<BigInteger>> me) -> new ManagerBehavior.InstructionCommand("start", me),
                Duration.ofSeconds(20),
                system.scheduler()
        );
        result.whenComplete((res, err) -> {
            if ( res != null ) {
                res.forEach(System.out::println);
            } else {
                System.out.println("Timed out");
            }
            system.terminate();
        });
    }
}
