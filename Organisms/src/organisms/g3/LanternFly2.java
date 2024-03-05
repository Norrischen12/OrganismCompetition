package organisms.g3;

import organisms.Constants;
import organisms.Move;
import organisms.OrganismsPlayer;
import organisms.ui.OrganismsGame;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class LanternFly2  {
    private OrganismsGame game;
    private int dna;
    private ThreadLocalRandom random;

    // keep a counter of the number of moves
    private int moves;

    public void setInfo(OrganismsGame game, int dna) throws Exception {
        this.game = game;
        this.dna = dna;
        this.random = ThreadLocalRandom.current();
        this.moves = 0;
    }

    private List<Integer> emptyNeighboringCells(int neighborN, int neighborS, int neighborW, int neighborE) {
        List<Integer> neighbors = new ArrayList<>();
        //If there's no neighbors, we can consider moving to that spot
        if(neighborN == -1) {
            neighbors.add(3);
        }
        if(neighborS == -1) {
            neighbors.add(4);
        }
        if(neighborW == -1) {
            neighbors.add(1);
        }
        if(neighborE == -1) {
            neighbors.add(2);
        }
        return neighbors;
    }

    private int randomAvailableDirectionIndex(List<Integer> directions) {
        return this.random.nextInt(directions.size());
    }

    private List<Integer> directionsWithFood(boolean foodN, boolean foodS, boolean foodW, boolean foodE) {
        List<Integer> foodNeighbors = new ArrayList<>();
        if(foodN) {
            foodNeighbors.add(3);
        }
        if(foodS) {
            foodNeighbors.add(4);
        }
        if(foodW) {
            foodNeighbors.add(1);
        }
        if(foodE) {
            foodNeighbors.add(2);
        }
        return foodNeighbors;
    }

    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW, int dna, OrganismsGame game, ThreadLocalRandom random) {


        moves++;
        Constants.Action stay = Constants.Action.fromInt(0);
        this.dna = dna;
        this.random = random;

        int reproduceEnergyThreshold = 330;
        int moveFrequency = 20;

        if (this.dna < 0) {
            this.dna = 120;
        }

        List<Integer> directions = emptyNeighboringCells(neighborN, neighborS, neighborW, neighborE);
        if (directions.isEmpty()) {
            return Move.movement(Constants.Action.fromInt(0));
        }

        int randomAvailableDirection = directions.get(randomAvailableDirectionIndex(directions));

        List<Integer> directionsWithFood = directionsWithFood(foodN, foodS, foodW, foodE);
        if (!directionsWithFood.isEmpty()) {
            randomAvailableDirection = directionsWithFood.get(randomAvailableDirectionIndex(directionsWithFood));
        }

        if (energyLeft > reproduceEnergyThreshold) {
            Constants.Action childPosition = Constants.Action.fromInt(randomAvailableDirection);
            //for now, make it so that the child in default has the same dna as parent
            return Move.reproduce(childPosition, this.dna);
        }

        if(foodHere > 4) {
            return Move.movement(stay);
        }

        if(!directionsWithFood.isEmpty()) {
            return Move.movement(Constants.Action.fromInt(randomAvailableDirection));
        }

        if (moves % moveFrequency == 0 && moves < 150) {
            return Move.movement(Constants.Action.fromInt(randomAvailableDirection));
        }

        return Move.movement(Constants.Action.fromInt(0));
//
    }


    public int getDNA() {
        return this.dna;
    }

    public int externalState() {
        return dna;
    }
}
