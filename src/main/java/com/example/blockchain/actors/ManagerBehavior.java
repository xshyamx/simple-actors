package com.example.blockchain.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
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
    private ManagerBehavior(ActorContext<Command> context) {
        super(context);
    }
    public static Behavior<Command> create() {
        return Behaviors.setup(ManagerBehavior::new);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(MineBlockCommand.class, cmd -> {
                    block = cmd.getBlock();
                    sender = cmd.getSender();
                    difficultyLevel = cmd.getDifficultyLevel();
                    for( int i = 0; i < 10; i++ ) {
                        startNextWorker();
                    }
                    return Behaviors.same();
                })
                .onMessage(HashResultCommand.class, cmd -> {
                    return Behaviors.same();
                })
                .build();
    }

    private ActorRef<HashResult> sender;
    private Block block;
    private int difficultyLevel;
    private int currentNonce = 0 ;

    private void startNextWorker() {
        ActorRef<WorkerBehavior.Command> worker = getContext().spawn(
                WorkerBehavior.create(),
                "worker" + currentNonce
        );
        worker.tell(new WorkerBehavior.Command(block, currentNonce++, difficultyLevel, getContext().getSelf()));
    }
}
