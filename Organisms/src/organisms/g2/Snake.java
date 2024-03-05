package organisms.g2;

import organisms.Move;
import organisms.OrganismsPlayer;
import organisms.ui.OrganismsGame;

import java.awt.*;

public class Snake implements OrganismsPlayer {
    private OrganismsGame game;
    private int dna;
    private int reproduceOrMoveConsumption;
    private Point position;
    private int u;
    private int M;
    private int K;
    private int moveCount = 0;  // Move counter

    @Override
    public void register(OrganismsGame game, int dna) throws Exception {
        this.game = game;
        this.dna = dna;
        this.reproduceOrMoveConsumption = game.v();
        this.u = game.u();
        this.M = game.M();
        this.K = game.K();
    }

    @Override
    public String name() {
        return "Snake";
    }

    @Override
    public Color color() {
        return new Color(4, 204, 0, 225);
    }

    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW) {
        moveCount++;  // Increment moves every turn

        // Dynamic reproduction threshold, decreasing as moveCount increases
        double initialThreshold = M / 2.0;  // Initial threshold
        double minThreshold = M / 10.0;     // Minimum threshold
        double decreaseFactor = (initialThreshold - minThreshold) / (double) K;
        double reproductionThreshold = initialThreshold - (decreaseFactor * moveCount);
        reproductionThreshold = Math.max(reproductionThreshold, minThreshold);  // Ensure it doesn't go below minThreshold

        // When about to die, prioritize moving towards food.
        if (energyLeft < reproduceOrMoveConsumption * 4) {
            if (foodN) return Move.movement(Action.NORTH);
            if (foodE) return Move.movement(Action.EAST);
            if (foodS) return Move.movement(Action.SOUTH);
            if (foodW) return Move.movement(Action.WEST);
            return Move.movement(Action.EAST);
        }

        if (foodHere > 0 && (((energyLeft - this.reproduceOrMoveConsumption))) > reproductionThreshold) {
            if (foodN && neighborN == -1) {
                return Move.reproduce(Action.NORTH, dna);
            }
            if (foodS && neighborS == -1) {
                return Move.reproduce(Action.SOUTH, dna);
            }
            if (foodE && neighborE == -1) {
                return Move.reproduce(Action.EAST, dna);
            }
            if (foodW && neighborW == -1) {
                return Move.reproduce(Action.WEST, dna);
            }
        } else if ((energyLeft - this.reproduceOrMoveConsumption) > 0) {
            if (foodN && neighborN == -1) {
                return Move.movement(Action.NORTH);
            }
            if (foodS && neighborS == -1) {
                return Move.movement(Action.SOUTH);
            }
            if (foodE && neighborE == -1) {
                return Move.movement(Action.EAST);
            }
            if (foodW && neighborW == -1) {
                return Move.movement(Action.WEST);
            }
        }

        return Move.movement(Action.STAY_PUT);
    }


    @Override
    public int externalState() {
        return 0;
    }
}
