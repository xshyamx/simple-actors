package com.example.blockchain.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.*;
import com.example.blockchain.model.Block;
import com.example.blockchain.model.HashResult;

import java.io.Serializable;
import java.util.Objects;

public class ManagerBehavior extends AbstractBehavior<ManagerBehavior.Command> {
    public interface Command extends Serializable { }

    public static class MineBlockCommand implements Command {
        private static final long serialVersionUID = 1L;
        private final Block block;
        private final ActorRef<HashResult> sender;
        private final int difficultyLevel;

        public MineBlockCommand(Block block, ActorRef<HashResult> sender, int difficultyLevel) {
            this.block = block;
            this.sender = sender;
            this.difficultyLevel = difficultyLevel;
        }

        public Block getBlock() {
            return block;
        }

        public ActorRef<HashResult> getSender() {
            return sender;
        }

        public int getDifficultyLevel() {
            return difficultyLevel;
        }
    }
    public static class HashResultCommand implements Command {
        public static final long serialVersionUID = 1L;

        private final HashResult result;

        public HashResultCommand(HashResult result) {
            this.result = result;
        }

        public HashResult getResult() {
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HashResultCommand that = (HashResultCommand) o;
            return Objects.equals(result, that.result);
        }

        @Override
        public int hashCode() {
            return Objects.hash(result);
        }
    }
    private StashBuffer<Command> stashBuffer;

    private ManagerBehavior(ActorContext<Command> context, StashBuffer<Command> stashBuffer) {
        super(context);
        this.stashBuffer = stashBuffer;
    }

    public static Behavior<Command> create() {
        return Behaviors.withStash(5, stash -> {
            return Behaviors.setup(ctx -> new ManagerBehavior(ctx, stash));
        });
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(MineBlockCommand.class, cmd -> {
                    block = cmd.getBlock();
                    sender = cmd.getSender();
                    difficultyLevel = cmd.getDifficultyLevel();
                    curentlyMining = true;
                    for( int i = 0; i < 10; i++ ) {
                        startNextWorker();
                    }
                    return Behaviors.same();
                })
                .onMessage(HashResultCommand.class, cmd -> {
                    curentlyMining = false;
                    getContext().getChildren().forEach(getContext()::stop);
                    sender.tell(cmd.getResult());
                    return Behaviors.same();
                })
                .onSignal(Terminated.class, handler -> {
                    startNextWorker();
                    return Behaviors.same();
                })
                .build();
    }

    private Receive<Command> idleMessageHandler() {
        return newReceiveBuilder()
                .onMessage(MineBlockCommand.class, cmd -> {
                    block = cmd.getBlock();
                    sender = cmd.getSender();
                    difficultyLevel = cmd.getDifficultyLevel();
                    curentlyMining = true;
                    for( int i = 0; i < 10; i++ ) {
                        startNextWorker();
                    }
                    return Behaviors.same();
                })
                .onSignal(Terminated.class, handler -> Behaviors.same())
                .build();
    }

    private Receive<Command> activeMessageHandler() {
        return newReceiveBuilder()
                .onMessage(HashResultCommand.class, cmd -> {
                    curentlyMining = false;
                    getContext().getChildren().forEach(getContext()::stop);
                    sender.tell(cmd.getResult());
                    return stashBuffer.unstashAll(idleMessageHandler());
                })  
                .onSignal(Terminated.class, handler -> {
                    startNextWorker();
                    return Behaviors.same();
                })
                .onMessage(MineBlockCommand.class, cmd -> {
                    stashBuffer.stash(cmd);
                    return Behaviors.same();
                })
                .build();
    }

    private ActorRef<HashResult> sender;
    private Block block;
    private int difficultyLevel;
    private int currentNonce = 0 ;
    private boolean curentlyMining;

    private void startNextWorker() {
        if ( curentlyMining ) {
//            System.out.println("Starting worker with nonce " + currentNonce * 1000);
            Behavior<WorkerBehavior.Command> workerBehavior =
                    Behaviors.supervise(WorkerBehavior.create())
                            .onFailure(SupervisorStrategy.resume());

            ActorRef<WorkerBehavior.Command> worker = getContext().spawn(
                    workerBehavior,
                    "worker" + currentNonce
            );
            getContext().watch(worker);
            worker.tell(new WorkerBehavior.Command(block, currentNonce * 1000, difficultyLevel, getContext().getSelf()));
            currentNonce++;
        }
    }
}
