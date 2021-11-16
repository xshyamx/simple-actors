package com.example.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.Duration;
import java.util.SortedSet;
import java.util.TreeSet;

public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command> {
    public interface Command extends Serializable {
    }
    public static class InstructionCommand implements Command {
        private final long serialVersionUID = 1L;
        private String message;
        private ActorRef<SortedSet<BigInteger>> sender;

        public InstructionCommand(String message, ActorRef<SortedSet<BigInteger>> sender) {
            this.message = message;
            this.sender = sender;
        }

        public String getMessage() {
            return message;
        }

        public ActorRef<SortedSet<BigInteger>> getSender() {
            return sender;
        }
    }
    public static class ResultCommand implements Command {
        private final long serialVersionUID = 1L;
        private BigInteger prime;

        public ResultCommand(BigInteger prime) {
            this.prime = prime;
        }

        public BigInteger getPrime() {
            return prime;
        }
    }
    private class NoResponseCommand implements Command {
        private final long serialVersionUID = 1L;
        private ActorRef<WorkerBehavior.Command> worker;

        public NoResponseCommand(ActorRef<WorkerBehavior.Command> worker) {
            this.worker = worker;
        }

        public ActorRef<WorkerBehavior.Command> getWorker() {
            return worker;
        }
    }

    private ManagerBehavior(ActorContext<Command> context) {
        super(context);
    }
    public static Behavior<Command> create() {
        return Behaviors.setup(ManagerBehavior::new);
    }

    private SortedSet<BigInteger> primes = new TreeSet<>();
    private ActorRef<SortedSet<BigInteger>> sender;

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(InstructionCommand.class, cmd -> {
                    if ( "start".equals(cmd.message) ) {
                        sender = cmd.sender;
                        for ( int i = 0; i < 20; i++ ) {
                            ActorRef<WorkerBehavior.Command> ref = getContext().spawn(WorkerBehavior.create(), String.format("worker-%02d", i));
                            askWorkerForPrime(ref);
                        }
                    }
                    return Behaviors.same();
                })
                .onMessage(ResultCommand.class, cmd -> {
                    primes.add(cmd.prime);
                    System.out.println("Got " + primes.size() + " primes");
                    if ( primes.size() == 20 ) {
                        primes.forEach(System.out::println);
                        // send back result
                        sender.tell(primes);
//                        return Behaviors.stopped();
                    }
                    return Behaviors.same();
                })
                .onMessage(NoResponseCommand.class, cmd -> {
                    System.out.println("Retrying for " + cmd.getWorker().path());
                    askWorkerForPrime(cmd.getWorker());
                    return Behaviors.same();
                })
                .build();
    }

    private void askWorkerForPrime(ActorRef<WorkerBehavior.Command> worker) {
        getContext().ask(
                Command.class,
                worker,
                Duration.ofSeconds(5),
                (me) -> new WorkerBehavior.Command("start", me),
                (res, err) -> {
                    if ( res != null ) {
                        return res;
                    } else {
                        return new NoResponseCommand(worker);
                    }
                }
        );
    }

}
