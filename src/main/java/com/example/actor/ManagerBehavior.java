package com.example.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.SortedSet;
import java.util.TreeSet;

public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command> {
    private ManagerBehavior(ActorContext<Command> context) {
        super(context);
    }
    public static Behavior<Command> create() {
        return Behaviors.setup(ManagerBehavior::new);
    }

    private SortedSet<BigInteger> primes = new TreeSet<>();

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(InstructionCommand.class, cmd -> {
                    if ( "start".equals(cmd.message) ) {
                        for ( int i = 0; i < 20; i++ ) {
                            ActorRef<WorkerBehavior.Command> ref = getContext().spawn(WorkerBehavior.create(), String.format("worker-%02d", i));
                            ref.tell(new WorkerBehavior.Command("start", getContext().getSelf()));
                            ref.tell(new WorkerBehavior.Command("start", getContext().getSelf()));
                        }
                    }
                    return Behaviors.same();
                })
                .onMessage(ResultCommand.class, cmd -> {
                    primes.add(cmd.prime);
                    System.out.println("Got " + primes.size() + " primes");
                    if ( primes.size() == 20 ) {
                        getContext().getSystem().terminate();
                        primes.forEach(System.out::println);
                    }
                    return Behaviors.same();
                })
                .build();
    }
    public interface Command extends Serializable {
        public static final long serialVersionUID = 1L;
    }
    public static class InstructionCommand implements Command {
        private String message;

        public InstructionCommand(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
    public static class ResultCommand implements Command {
        private BigInteger prime;

        public ResultCommand(BigInteger prime) {
            this.prime = prime;
        }

        public BigInteger getPrime() {
            return prime;
        }
    }
}
