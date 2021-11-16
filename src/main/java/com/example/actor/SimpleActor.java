package com.example.actor;

import akka.actor.typed.ActorSystem;

public class SimpleActor {
    public static void main(String[] args) throws InterruptedException {
        ActorSystem<String> system = ActorSystem.create(SimpleBehavior.create(), "actor-system");
        system.tell("Hello are you there?");
        system.tell("Second message");
        system.tell("say-hello");
        system.tell("create-child");
        system.tell("kill");
    }
}
