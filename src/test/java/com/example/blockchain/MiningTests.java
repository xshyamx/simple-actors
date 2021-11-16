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
        assertEquals(1, logs.size());
        assertEquals("null", logs.get(0).message());
        assertEquals(Level.DEBUG, logs.get(0).level());
    }
    @Test
    void nonceInRange() {
        var actor = BehaviorTestKit.create(WorkerBehavior.create());
        Block block = BlocksData.getNextBlock(0, "0");
        WorkerBehavior.Command message = new WorkerBehavior.Command(block, 4385400, 5);
        actor.run(message);
        var logs = actor.getAllLogEntries();
        String expectedResult = "4385438 : 000005063c2755396873ec402b09e910c46791dd06acb720cb6ca392ed6e613f";
        assertEquals(1, logs.size());
        assertEquals(expectedResult, logs.get(0).message());
        assertEquals(Level.DEBUG, logs.get(0).level());
    }
}
