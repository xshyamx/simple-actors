package com.example.blockchain;
import java.time.Duration;
import java.util.concurrent.CompletionStage;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import com.example.blockchain.actors.ManagerBehavior;
import com.example.blockchain.actors.MiningSystemBehavior;
import com.example.blockchain.model.Block;
import com.example.blockchain.model.BlockChain;
import com.example.blockchain.model.BlockValidationException;
import com.example.blockchain.model.HashResult;
import com.example.blockchain.utils.BlocksData;

public class BlockChainMiner {
	
	int difficultyLevel = 5;
	BlockChain blocks = new BlockChain();
	long start = System.currentTimeMillis();
	ActorSystem<ManagerBehavior.Command> actorSystem;

	private void mineNextBlock() {
		int nextBlockId = blocks.getSize();
		if (nextBlockId < 10) {
			String lastHash = nextBlockId > 0 ? blocks.getLastHash() : "0";
			Block block = BlocksData.getNextBlock(nextBlockId, lastHash);
			CompletionStage<HashResult> results = AskPattern.ask(actorSystem,
					me -> new ManagerBehavior.MineBlockCommand(block, me, 5),
					Duration.ofSeconds(30),
					actorSystem.scheduler());
				
			results.whenComplete( (reply,failure) -> {
				
				if (reply == null || !reply.isComplete()) {
					System.out.println("ERROR: No valid hash was found for a block");
				}
				
				block.setHash(reply.getHash());
				block.setNonce(reply.getNonce());
				
				try {
					blocks.addBlock(block);
					System.out.println("Block added with hash : " + block.getHash());
					System.out.println("Block added with nonce: " + block.getNonce());
					mineNextBlock();
				} catch (BlockValidationException e) {
					System.out.println("ERROR: No valid hash was found for a block");
				}
			});
			
		}
		else {
			Long end = System.currentTimeMillis();
			actorSystem.terminate();
			blocks.printAndValidate();
			System.out.println("Time taken " + (end - start) + " ms.");
		}
	}

	public void mineIndependentBlock() {
		Block block = BlocksData.getNextBlock(7, "12345");
		CompletionStage<HashResult> result = AskPattern.ask(
				actorSystem,
				(ActorRef<HashResult> me) -> new ManagerBehavior.MineBlockCommand(block, me, 5),
				Duration.ofSeconds(30),
				actorSystem.scheduler()
		);
		result.whenComplete((reply, err) -> {
			if ( reply == null ) {
				System.out.println("No has found");
			} else {
				System.out.println("All is fine");
			}
		});
	}
	public void mineBlocks() {
		
		actorSystem = ActorSystem.create(MiningSystemBehavior.create(), "BlockChainMiner");
		mineNextBlock();
		mineIndependentBlock();
	}
	
}
