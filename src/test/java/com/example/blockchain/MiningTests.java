package com.example.blockchain;

import akka.actor.testkit.typed.javadsl.BehaviorTestKit;
import akka.actor.testkit.typed.javadsl.TestInbox;
import akka.actor.typed.Behavior;
import com.example.blockchain.actors.ManagerBehavior;
import com.example.blockchain.actors.WorkerBehavior;
import com.example.blockchain.model.Block;
import com.example.blockchain.model.HashResult;
import com.example.blockchain.utils.BlocksData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

import static org.junit.jupiter.api.Assertions.*;

public class MiningTests {
    BehaviorTestKit<WorkerBehavior.Command> actor;
    TestInbox<ManagerBehavior.Command> inbox;
    Block block;
    @BeforeEach
    public void beforeEach() {
        actor = BehaviorTestKit.create(WorkerBehavior.create());
        inbox = TestInbox.create();
        block = BlocksData.getNextBlock(0, "0");
    }
    private WorkerBehavior.Command getCommand(int startNonce, int difficulty) {
        return new WorkerBehavior.Command(block, startNonce, difficulty, inbox.getRef());
    }
    @Test
    void nonceNotInRange() {
        actor.run(getCommand(0, 5));
        var logs = actor.getAllLogEntries();
        assertEquals(1, logs.size());
        assertEquals("null", logs.get(0).message());
        assertEquals(Level.DEBUG, logs.get(0).level());
    }
    @Test
    void nonceInRange() {
        actor.run(getCommand(4385400, 5));
        var logs = actor.getAllLogEntries();
        String expectedResult = "4385438 : 000005063c2755396873ec402b09e910c46791dd06acb720cb6ca392ed6e613f";
        assertEquals(1, logs.size());
        assertEquals(expectedResult, logs.get(0).message());
        assertEquals(Level.DEBUG, logs.get(0).level());
    }

    @Test
    void nonceInRangeWithMessage() {
        actor.run(getCommand(4385400, 5));
        HashResult expected = new HashResult();
        expected.foundAHash("000005063c2755396873ec402b09e910c46791dd06acb720cb6ca392ed6e613f", 4385438);

        inbox.expectMessage(new ManagerBehavior.HashResultCommand(expected));
    }

    @Test
    void nonceNotInRangeWithMessage() {
        actor.run(getCommand(0, 5));
        assertFalse(inbox.hasMessages());
    }
}
