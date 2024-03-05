package organisms.g2;

import organisms.Move;
import organisms.OrganismsPlayer;
import organisms.ui.OrganismsGame;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

public class Rabbit implements OrganismsPlayer {
    private OrganismsGame game;
    private int dna;
    private int reproduceOrMoveConsumption;
    private Point position;
    private int u;
    private int M;
    private int K;

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
        return "Rabbit";
    }

    @Override
    public Color color() {
        return new Color(204, 204, 0, 225);
    }

    @Override
    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW) {
        if (foodHere > 0 && (((energyLeft - this.reproduceOrMoveConsumption) / 2.0)) > M / 5.0) {
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
