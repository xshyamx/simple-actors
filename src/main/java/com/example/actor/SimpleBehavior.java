package com.example.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class SimpleBehavior extends AbstractBehavior<String> {
    private SimpleBehavior(ActorContext<String> context) {
        super(context);
    }

    public static Behavior<String> create() {
        return Behaviors.setup(context -> new SimpleBehavior(context));
    }

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder()
                .onMessageEquals("kill", () -> {
                    System.out.println("kill : " + getContext().getSelf().path());
                    System.exit(1);
                    return this;
                })
                .onMessageEquals("say-hello", () -> {
                    System.out.println(getContext().getSelf().path() + " : Hello World!");
                    return this;
                })
                .onMessageEquals("create-child", () -> {
                    ActorRef<String> ref = getContext().spawn(SimpleBehavior.create(), "child");
                    ref.tell("say-hello");
                    return this;
                })
                .onAnyMessage(message -> {
                    System.out.println("Received message : " + message);
                    return this;
                })
                .build();
    }
}
