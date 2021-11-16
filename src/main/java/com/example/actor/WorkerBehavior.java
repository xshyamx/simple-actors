package com.example.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Random;

public class WorkerBehavior extends AbstractBehavior<WorkerBehavior.Command> {
    private WorkerBehavior(ActorContext<Command> context) {
        super(context);
    }
    public static Behavior<Command> create() {
        return Behaviors.setup(WorkerBehavior::new);
    }
    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onAnyMessage(cmd -> {
                    if ( "start".equals(cmd.message) ) {
                        BigInteger i = new BigInteger(2000, new Random());
                        System.out.println(i.nextProbablePrime());
                    }
                    return this;
                })
                .build();
    }
    public static class Command implements Serializable {
        private String message;
        private ActorRef<String> sender;
        public static final long serialVersionUID = 1;

        public Command(String message, ActorRef<String> sender) {
            this.message = message;
            this.sender = sender;
        }

        public String getMessage() {
            return message;
        }

        public ActorRef<String> getSender() {
            return sender;
        }
    }
}
