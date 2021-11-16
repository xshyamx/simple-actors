package com.example.blockchain.actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.Behaviors;
import com.example.blockchain.model.Block;
import com.example.blockchain.model.HashResult;

import static com.example.blockchain.utils.BlockChainUtils.calculateHash;

public class WorkerBehavior extends AbstractBehavior<WorkerBehavior.Command> {
	
	public static class Command {
		private Block block;
		private int startNonce;
		private int difficulty;
		
		public Command(Block block, int startNonce, int difficulty) {
			this.block = block;
			this.startNonce = startNonce;
			this.difficulty = difficulty;
		}
		
		public Block getBlock() {
			return block;
		}
		public int getStartNonce() {
			return startNonce;
		}
		public int getDifficulty() {
			return difficulty;
		}
		
	}

	private WorkerBehavior(ActorContext<Command> context) {
		super(context);
	}
	
	public static Behavior<Command> create() {
		return Behaviors.setup(WorkerBehavior::new);
	}

	@Override
	public Receive<Command> createReceive() {
		// TODO Auto-generated method stub
		return newReceiveBuilder()
				.onAnyMessage(cmd -> {
					Block block = cmd.getBlock();
					int difficultyLevel = cmd.getDifficulty(),
							startNonce = cmd.getStartNonce(),
							endNonce = startNonce + 1000;

					String hash = new String(new char[difficultyLevel]).replace("\0", "X");
					String target = new String(new char[difficultyLevel]).replace("\0", "0");

					int nonce = startNonce;
					while (!hash.substring(0,difficultyLevel).equals(target) && nonce < endNonce) {
						nonce++;
						String dataToEncode = block.getPreviousHash() + Long.toString(block.getTransaction().getTimestamp()) + Integer.toString(nonce) + block.getTransaction();
						hash = calculateHash(dataToEncode);
					}
					if (hash.substring(0,difficultyLevel).equals(target)) {
						HashResult hashResult = new HashResult();
						hashResult.foundAHash(hash, nonce);
						getContext().getLog().debug(hashResult.getNonce() +  " : " + hashResult.getHash());
						return Behaviors.same();
					}
					else {
						getContext().getLog().debug("null");
						return Behaviors.same();
					}
				})
				.build();
	}


}
