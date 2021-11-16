package com.example.race;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class RaceController extends AbstractBehavior<RaceController.Command> {

    private Object TIMER_KEY;

    public static Command start() {
        return new StartCommand();
    }
    public static Command raceUpdate(ActorRef<Racer.Command> racer, int position) {
        return new RaceUpdateCommand(racer, position);
    }

    public static Command done(ActorRef<Racer.Command> racer) {
        return new RacerCompleteCommand(racer);
    }

    public interface Command extends Serializable {
    }
    public static class StartCommand implements Command {
        private static final long serialVersionUID = 1L;
    }
    public static class RaceUpdateCommand implements Command {
        private static final long serialVersionUID = 1L;
        private ActorRef<Racer.Command> controller;
        private int position;

        private RaceUpdateCommand(ActorRef<Racer.Command> controller, int position) {
            this.controller = controller;
            this.position = position;
        }

        public ActorRef<Racer.Command> getController() {
            return controller;
        }

        public int getPosition() {
            return position;
        }
    }
    public static class RacerCompleteCommand implements Command {
        private static final long serialVersionUID = 1L;
        private ActorRef<Racer.Command> racer;

        private RacerCompleteCommand(ActorRef<Racer.Command> racer) {
            this.racer = racer;
        }

        public ActorRef<Racer.Command> getRacer() {
            return racer;
        }
    }
    private class RaceCompleteCommand implements Command {
        private static final long serialVersionUID = 1L;
    }
    private class TickCommand implements Command {
        private static final long serialVersionUID = 1L;

    }

    private RaceController(ActorContext<Command> context) {
        super(context);
    }

    public static Behavior<Command> create() {
        return Behaviors.setup(RaceController::new);
    }

    private Map<ActorRef<Racer.Command>, Integer> currentPosition;
    private Map<ActorRef<Racer.Command>, Long> finishTimes;
    private long start;
    private int raceLength = 100;
    public static final int DEFAULT_RACERS = 10;
    private int racers;
    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartCommand.class, cmd -> {
                    start = System.currentTimeMillis();
                    currentPosition = new HashMap<>();
                    finishTimes = new HashMap<>();
                    racers = DEFAULT_RACERS;
                    for ( int i = 0; i < racers; i++ ) {
                        ActorRef<Racer.Command> racer = getContext().spawn(Racer.create(), "racer" + i);
                        currentPosition.put(racer, 0);
                        racer.tell(Racer.startCommand(raceLength));
                    }
                    return Behaviors.withTimers(timers -> {
                        timers.startTimerAtFixedRate(TIMER_KEY, new TickCommand(), Duration.ofSeconds(1));
                        return Behaviors.same();
                    });
                })
                .onMessage(TickCommand.class, cmd -> {
                    currentPosition.keySet().forEach(racer -> {
                        racer.tell(Racer.positionCommand(getContext().getSelf()));
                    });
                    displayRace();
                    return Behaviors.same();
                })
                .onMessage(RacerCompleteCommand.class, cmd -> {
                    racers--;
                    finishTimes.put(cmd.getRacer(), System.currentTimeMillis() - start);
                    if ( racers == 0 ) {
                        getContext().getSelf().tell(new RaceCompleteCommand());
                    }
                    return Behaviors.same();
                })
                .onMessage(RaceUpdateCommand.class, cmd -> {
                    currentPosition.put(cmd.getController(), cmd.getPosition());
                    return Behaviors.same();
                })
                .onMessage(RaceCompleteCommand.class, cmd -> {
                    showResults();
                    System.out.printf("Race ended in %.2f seconds\n", (float) (System.currentTimeMillis() - start) / 1000);
                    currentPosition.keySet().forEach(getContext()::stop);
                    return Behaviors.withTimers(timers -> {
                        timers.cancelAll();
                        return Behaviors.stopped();
                    });
                })
                .build();
    }
    private void displayRace() {
        System.out.println("Race running for " + (System.currentTimeMillis() - start) + " ms");
        int i = 0;
        for ( ActorRef<Racer.Command> actor : currentPosition.keySet() ) {
            System.out.printf("%02d [%3d] : %s\n", ++i, currentPosition.get(actor), new String(new char[currentPosition.get(actor)]).replace('\0', '='));
        }
    }
    private void showResults() {
        System.out.println("Race Results");
        finishTimes.values().stream().sorted().forEach(tt -> {
            for ( ActorRef<Racer.Command> ref : finishTimes.keySet() ) {
                if ( finishTimes.get(ref) == tt ) {
                    String racerId = ref.path().name().replace("racer", "");
                    System.out.printf("Racer %s finished in %.2f seconds\n", racerId, ((float) tt/1000));
                }
            }
        });
    }
}
