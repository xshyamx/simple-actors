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
        return initial();
    }
    public Receive<Command> initial() {
        return newReceiveBuilder()
                .onAnyMessage(cmd -> {
                    BigInteger prime = new BigInteger(2000, new Random()).nextProbablePrime();
                    cmd.sender.tell(new ManagerBehavior.ResultCommand(prime));
                    return cached(prime);
                })
                .build();
    }

    private Receive<Command> cached(BigInteger prime) {
        return newReceiveBuilder()
                .onAnyMessage(cmd -> {
                    cmd.sender.tell(new ManagerBehavior.ResultCommand(prime));
                    return Behaviors.same();
                })
                .build();
    }

    public static class Command implements Serializable {
        private String message;
        private ActorRef<ManagerBehavior.Command> sender;
        public static final long serialVersionUID = 1;

        public Command(String message, ActorRef<ManagerBehavior.Command> sender) {
            this.message = message;
            this.sender = sender;
        }

        public String getMessage() {
            return message;
        }

        public ActorRef<ManagerBehavior.Command> getSender() {
            return sender;
        }
    }
}
