package organisms.g0;

import organisms.Move;
import organisms.ui.OrganismsGame;
import organisms.OrganismsPlayer;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

public class RandomPlayer implements OrganismsPlayer {
    private OrganismsGame game;
    private int dna;
    private ThreadLocalRandom random;

    @Override
    public void register(OrganismsGame game, int dna) throws Exception {
        this.game = game;
        this.dna = dna;
        this.random = ThreadLocalRandom.current();
    }

    @Override
    public String name() {
        return "G0 Random Player 0";
    }

    @Override
    public Color color() {
        return new Color(166, 124, 255, 255);
    }

    @Override
    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW) {
        // Moves are totally random and may not be valid depending on whether
        // neighboring squares are occupied!
        int actionIndex = this.random.nextInt(Action.getNumActions());
        Action actionChoice = Action.fromInt(actionIndex);

        if (actionChoice == Action.REPRODUCE) {
            // randomly pick a direction and key for the child
            int childPosIndex = this.random.nextInt(1, 5);
            Action childPosChoice = Action.fromInt(childPosIndex);
            int childKey = this.random.nextInt();
            return Move.reproduce(childPosChoice, childKey);
        } else {
            // staying put or moving in a direction
            return Move.movement(actionChoice);
        }
    }

    @Override
    public int externalState() {
        return 0;
    }
}
