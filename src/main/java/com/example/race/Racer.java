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

    private static Random random = new Random();

    @Override
    public Receive<Command> createReceive() {
        return notYetStarted();
    }
    Receive<Command> notYetStarted() {
        return newReceiveBuilder()
                .onMessage(StartCommand.class, cmd -> {
                    int currentSpeed = random.nextInt(10) + 5;
                    return running(cmd.getRaceLength(), currentSpeed, 0);
                })
                .onMessage(PositionCommand.class, cmd -> {
                    cmd.getController().tell(RaceController.raceUpdate(getContext().getSelf(), 0));
                    return Behaviors.same();
                })
                .build();
    }

    private Receive<Command> running(int raceLength, int currentSpeed, int currentPosition) {
        return newReceiveBuilder()
                .onMessage(PositionCommand.class, cmd -> {
                    int nextPosition = currentPosition + currentSpeed;
                    if ( nextPosition >= raceLength ) {
                        cmd.getController().tell(RaceController.raceUpdate(getContext().getSelf(), raceLength));
                        cmd.getController().tell(RaceController.done(getContext().getSelf()));
                        return completed(raceLength);
                    }
                    cmd.getController().tell(RaceController.raceUpdate(getContext().getSelf(), currentPosition));
                    return running(raceLength, currentSpeed, nextPosition);
                })
                .build();
    }

    private Receive<Command> completed(int raceLength) {
        return newReceiveBuilder()
                .onMessage(PositionCommand.class, cmd -> {
                    cmd.getController().tell(RaceController.raceUpdate(getContext().getSelf(), raceLength));
                    return Behaviors.ignore();
                })
                .build();
    }

}
