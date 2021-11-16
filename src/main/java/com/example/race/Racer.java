package com.example.race;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;
import java.util.Random;

public class Racer extends AbstractBehavior<Racer.Command> {
    public static Command startCommand(int raceLength) {
        return new StartCommand(raceLength);
    }
    public static Command positionCommand(ActorRef<RaceController.Command> controller) {
        return new PositionCommand(controller);
    }
    public interface Command extends Serializable {
    }
    public static class StartCommand implements Command {
        private static final long serialVersionUID = 1L;
        private int raceLength;

        private StartCommand(int raceLength) {
            this.raceLength = raceLength;
        }

        public int getRaceLength() {
            return raceLength;
        }
    }
    public static class PositionCommand implements  Command {
        private static final long serialVersionUID = 1L;
        private ActorRef<RaceController.Command> controller;

        private PositionCommand(ActorRef<RaceController.Command> controller) {
            this.controller = controller;
        }

        public ActorRef<RaceController.Command> getController() {
            return controller;
        }
    }

    private Racer(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(Racer::new);
    }

    private Random random;
    private double currentSpeed = 0;
    private int currentPosition = 0;
    private int raceLength;
    private boolean done = false;

    private void determineNextSpeed() {
        currentPosition += currentSpeed;
    }
    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartCommand.class, cmd -> {
                    raceLength = cmd.getRaceLength();
                    random = new Random();
                    currentSpeed = random.nextInt(10) + 5;
                    return this;
                })
                .onMessage(PositionCommand.class, cmd -> {
                    if ( !done ) {
                        determineNextSpeed();
                        if ( currentPosition >= raceLength ) {
                            currentPosition = raceLength;
                            done = true;
                            cmd.getController().tell(RaceController.done(getContext().getSelf()));
                        }
                    }
                    cmd.getController().tell(RaceController.raceUpdate(getContext().getSelf(), (int) currentPosition));
                    return this;
                })
                .build();
    }
}
