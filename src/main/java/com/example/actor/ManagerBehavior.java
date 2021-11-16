package com.example.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class ManagerBehavior extends AbstractBehavior<String> {
    private ManagerBehavior(ActorContext<String> context) {
        super(context);
    }
    public static Behavior<String> create() {
        return Behaviors.setup(ManagerBehavior::new);
    }

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder()
                .onMessageEquals("start", () -> {
                    for ( int i = 0; i < 20; i++ ) {
                        ActorRef<String> ref = getContext().spawn(WorkerBehavior.create(), String.format("worker-%02d", i));
                        ref.tell("start");
                    }
                    return this;
                })
                .build();
    }
}
