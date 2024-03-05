package organisms.g0;

import organisms.Move;
import organisms.ui.OrganismsGame;
import organisms.OrganismsPlayer;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

public class Enthusiasm implements OrganismsPlayer {
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
        return "G5 Enthusiasm";
    }

    @Override
    public Color color() {
        return new Color(1, 50, 32, 255);
    }

    @Override
    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW) {

        int M = this.game.M();
        int u = this.game.u();

        double reproductionThreshold = M * 0.3;

        //reproduce if there is food on the cell and current energy levels are sufficiently high
        if (foodHere > 0) {
            if (energyLeft > reproductionThreshold) {
                //find best direction to reproduce in
                int bestDir = simpleFindBestMove(foodN, foodE, foodS, foodW, 
                                neighborN, neighborE, neighborS, neighborW);
                if (bestDir != 5) {
                    return Move.reproduce(Action.fromInt(bestDir), this.externalState());
                } 
                //if all adjacent cells are blocked stay put
                return Move.movement(Action.STAY_PUT);
            } else {
                return Move.movement(Action.STAY_PUT);
            }
        } else {
            //find the best direction to move in
            int bestDir = simpleFindBestMove(foodN, foodE, foodS, foodW, 
                                neighborN, neighborE, neighborS, neighborW);
            if (bestDir != 5) {
                return Move.movement(Action.fromInt(bestDir));
            } 
            //if all adjacent cells are blocked stay put
            return Move.movement(Action.STAY_PUT);
        }
        
    }


    @Override
    public int externalState() {
        return 99;
    }


    //a simple strategy for picking moves that prioritises top/left before bottom/right
    //does not acknowledge competiton/ other organisms
    public static int simpleFindBestMove(boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW) {

        if (neighborW == -1 & foodW) return 1;
        if (neighborN == -1 & foodN) return 3;
        if (neighborE == -1 & foodE) return 2;
        if (neighborS == -1 & foodS) return 4;

        if (neighborW == -1) return 1;
        if (neighborN == -1) return 3;
        if (neighborE == -1) return 2;
        if (neighborS == -1) return 4;


        return 5;

    }
}
