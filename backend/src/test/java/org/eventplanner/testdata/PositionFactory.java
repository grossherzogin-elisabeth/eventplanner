package org.eventplanner.testdata;

import java.util.List;

import org.eventplanner.events.domain.entities.positions.Position;

public class PositionFactory {

    public static List<Position> generateDefaultPositions() {
        return List.of(
            new Position(PositionKeys.CAPTAIN, "Captain", "#D9D9D9", 100, "Master"),
            new Position(PositionKeys.MATE, "Mate", "#D9D9D9", 90, "Mate"),
            new Position(PositionKeys.ENGINEER, "Engineer", "#FCE4D6", 80, "Engineer"),
            new Position(PositionKeys.DECKHAND, "Deckshand", "#E2EFDA", 10, "Deckshand"),
            new Position(PositionKeys.TRAINER, "Trainer", "#F4B084", 50, "Trainer")
        );
    }
}
