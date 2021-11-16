package com.example.race;

import akka.actor.typed.ActorSystem;

public class RaceSimulation {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create(RaceController.create(), "race-controller");
        system.tell(RaceController.start());
    }
}
