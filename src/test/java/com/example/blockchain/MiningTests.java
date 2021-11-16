package com.example.blockchain;

import akka.actor.testkit.typed.javadsl.BehaviorTestKit;
import com.example.blockchain.actors.WorkerBehavior;
import com.example.blockchain.model.Block;
import com.example.blockchain.utils.BlocksData;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class MiningTests {
    @Test
    void nonceNotInRange() {
        var actor = BehaviorTestKit.create(WorkerBehavior.create());
        Block block = BlocksData.getNextBlock(0, "0");
        WorkerBehavior.Command message = new WorkerBehavior.Command(block, 0, 5);
        actor.run(message);
        var logs = actor.getAllLogEntries();
        assertEquals(logs.size(), 1);
        assertEquals(logs.get(0).message(), "null");
        assertEquals(logs.get(0).level(), Level.DEBUG);
    }
}
