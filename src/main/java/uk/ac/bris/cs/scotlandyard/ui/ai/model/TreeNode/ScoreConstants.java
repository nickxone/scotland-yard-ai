package uk.ac.bris.cs.scotlandyard.ui.ai.model.TreeNode;

public final class ScoreConstants {

    public static final int WIN_SCORE = 100_000; // what Mr.X is gets if he wins
    public static final int LOSE_SCORE = -100_000; //  what Mr.X is gets if he loses

    public static final int DISTANCE_MULTIPLIER = 100; // how important for Mr.X be away from detectives
    public static final int SECRET_TICKET_BONUS = 50; // how important for Mr.X to hide its moves
    public static final int DOUBLE_TICKET_PENALTY_MULTIPLIER = 10; // penalty for early use of a double ticket

    // Strategic values of holding the following tickets
    public static final int TAXI_TICKET_VALUE = 200;
    public static final int BUS_TICKET_VALUE = 100;
    public static final int UNDERGROUND_TICKET_VALUE = 150;
    public static final int SECRET_TICKET_VALUE = 50;
    public static final int DOUBLE_TICKET_VALUE = 300;

    private ScoreConstants() {

    }
}
