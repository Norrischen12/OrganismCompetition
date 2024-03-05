package organisms.g3;

import organisms.Constants;
import organisms.Move;
import organisms.OrganismsPlayer;
import organisms.ui.OrganismsGame;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Chameleon implements OrganismsPlayer {
    private OrganismsGame game;
    private int dna;
    private ThreadLocalRandom random;

    LanternFly2 lanternfly = new LanternFly2();
    Desert2 desert = new Desert2();
    Rainforest2 rainforest = new Rainforest2();

    Color darkBlue = new Color(6, 44, 184, 255); // rainforest
    Color normalBlue = new Color(31, 81, 255, 255); // reg
    Color lightBlue = new Color(97, 131, 255, 255); // desert

    int numTurns = 0;
    Color currColor = lightBlue; // ensures that first spawned player is desert
    boolean justSwitched = false;
    boolean wasRainforest = false;
    int numNonRainforestTurns = 0; // # of consecutive turns where conditions have been worse than rainforest
    int numNonDefaultTurns = 0; // # of consecutive turns where conditions have been worse than default

    double foodEstimation; // on average, how much food is near the player
    double neighborsEstimation; // on average, how many organisms are near the player
    ArrayList<Integer> prevNeighbors = new ArrayList<>();
    ArrayList<Integer> prevFood = new ArrayList<>();

    int averageThreshold = 6; // how many previous turns' data we're averaging
    double rainforestThreshold = 3.4;
    double defaultThreshold = 0.8;
    int demoteThreshold = 40; // threshold of # of consecutive turns w/ worse conditions before organism type is "demoted"

    @Override
    public void register(OrganismsGame game, int dna) throws Exception {
        this.game = game;
        this.dna = dna;
        this.random = ThreadLocalRandom.current();
        desert.register(this.game, this.dna);
    }

    @Override
    public String name() { return "Lanternfly"; }

    @Override
    public Color color() {
        return currColor;
    }

    public void updateEstimations(int neighborN, int neighborE, int neighborS, int neighborW,
                                  int foodHere, boolean foodN, boolean foodE, boolean foodS,
                                  boolean foodW) {
        numTurns++;

        int currNeighbors = 0;
        int currFood = 0;

        if (Util.isNeighbor(neighborN)) {
            currNeighbors++;
        } if (Util.isNeighbor(neighborS)) {
            currNeighbors++;
        } if (Util.isNeighbor(neighborE)) {
            currNeighbors++;
        } if (Util.isNeighbor(neighborW)) {
            currNeighbors++;
        }

        if (foodHere > 0) {
            currFood++;
        } if (foodN) {
            currFood++;
        } if (foodS) {
            currFood++;
        } if (foodE) {
            currFood++;
        } if (foodW) {
            currFood++;
        }

        // Calculate averages

        prevNeighbors.add(currNeighbors);
        prevFood.add(currFood);
        // if enough turns have passed, delete the earliest remembered turn from list
        if (numTurns > averageThreshold) {
            prevNeighbors.remove(0);
            prevFood.remove(0);
        }
        int sumNeighbors = 0;
        for (int neighbor : prevNeighbors) {
            sumNeighbors += neighbor;
        }
        int sumFood = 0;
        for (int food : prevFood) {
            sumFood += food;
        }

        neighborsEstimation = (double) sumNeighbors / averageThreshold;
        foodEstimation = (double) sumFood / averageThreshold;

        // We weight food estimation by neighbors:
        // if there are lots of neighbors around us, incr food
        foodEstimation += 0.7 * neighborsEstimation;
    }

    @Override
    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE, boolean foodS, boolean foodW, int neighborN, int neighborE, int neighborS, int neighborW) throws Exception {

        updateEstimations(neighborN, neighborE, neighborS, neighborW, foodHere, foodN, foodE, foodS, foodW);
        Move move = Move.movement(Constants.Action.STAY_PUT);

        // if we're newly spawned
        if (numTurns <= 1) {
            if (Math.abs(this.dna) < 99 && this.dna != -1) { // desert
                currColor = darkBlue;
            }
            else if (this.dna < 200 && this.dna > 100) { // rainforest and lanternfly
                // this was supposed to be lightBlue, but it performs better this way
                currColor = normalBlue;
            }
            // for some reason, separating lanternfly's dna to be >200 and adding another if reduces performance
        }

        // if it's been demoteThreshold moves of worse conditions, demote
        if (numNonRainforestTurns >= demoteThreshold) {
            currColor = normalBlue;
        }
        if (numNonDefaultTurns >= demoteThreshold) {
            currColor = lightBlue;
        }

        // increase demote condition counters
        if (currColor.equals(darkBlue) && foodEstimation < rainforestThreshold) {
            numNonRainforestTurns++;
        }
        if (currColor.equals(normalBlue) && foodEstimation < defaultThreshold) {
            numNonDefaultTurns++;
        }
        // or reset demote condition counters (non consecutive)
        if (currColor.equals(darkBlue) && foodEstimation >= rainforestThreshold) {
            numNonRainforestTurns = 0;
        } else if (currColor.equals(normalBlue) && foodEstimation >= defaultThreshold) {
            numNonDefaultTurns = 0;
        }

        // switching functionality
        if (foodEstimation > rainforestThreshold || currColor == darkBlue) { // rainforest conditions
            if (currColor != darkBlue) {
                justSwitched = true;
            } else {
                justSwitched = false;
            }
            move = rainforest.move(foodHere, energyLeft, foodN, foodE, foodS, foodW, neighborN, neighborE, neighborS, neighborW, dna, game, random);
            this.dna = rainforest.getDNA();
            currColor = darkBlue;
        } else if (foodEstimation > defaultThreshold || currColor == normalBlue) { // regular conditions
            if (currColor != normalBlue) {
                justSwitched = true;
            } else {
                justSwitched = false;
            }
            move = lanternfly.move(foodHere, energyLeft, foodN, foodE, foodS, foodW, neighborN, neighborE, neighborS, neighborW, dna, game, random);
            this.dna = lanternfly.getDNA();
            currColor = normalBlue;
        } else { // desert conditions
            if (currColor != lightBlue) {
                justSwitched = true;
            } else {
                justSwitched = false;
            }
            move = desert.move(foodHere, energyLeft, foodN, foodE, foodS, foodW, neighborN, neighborE, neighborS, neighborW, dna, game, random);
            this.dna = desert.getDNA();
            currColor = lightBlue;
        }

        if (justSwitched) {
            this.dna = -1;
            numNonRainforestTurns = 0;
            numNonDefaultTurns = 0;
        }

        return move;
    }

    @Override
    public int externalState() throws Exception {
//        return dna;
        return desert.externalState();
    }
}