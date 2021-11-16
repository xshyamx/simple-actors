package com.example.blockchain.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.*;

public class MiningSystemBehavior extends AbstractBehavior<ManagerBehavior.Command> {
    PoolRouter<ManagerBehavior.Command> router;
    ActorRef<ManagerBehavior.Command> routerRef;
    private MiningSystemBehavior(ActorContext<ManagerBehavior.Command> context) {
        super(context);
        router = Routers.pool(3,
                Behaviors.supervise(ManagerBehavior.create()).onFailure(SupervisorStrategy.restart())
                        );
        routerRef = getContext().spawn(router, "manager-pool");
    }
    public static Behavior<ManagerBehavior.Command> create() {
        return Behaviors.setup(MiningSystemBehavior::new);
    }

    @Override
    public Receive<ManagerBehavior.Command> createReceive() {
        return newReceiveBuilder()
                .onAnyMessage(msg -> {
                    routerRef.tell(msg);
                    return Behaviors.same();
                })
                .build();
    }
}
