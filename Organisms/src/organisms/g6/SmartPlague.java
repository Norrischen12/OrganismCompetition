package organisms.g6;

import organisms.Move;
import organisms.OrganismsPlayer;
import organisms.ui.OrganismsGame;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SmartPlague implements OrganismsPlayer {

    private OrganismsGame game;
    private int dna;
    private ThreadLocalRandom random;
    private int moves;
    private int reproduceConsumption;
    private int M;

    private enum SEASON {
        DEFAULT, // Default is what we had originally
        WINTER, // Winter is for more stock up & conserve energy
        SPRING // Spring is aggressive reproduction

    }

    //keep another map with a count of each

    @Override
    public void register(OrganismsGame game, int dna) throws Exception {
        this.game = game;
        this.dna = dna;
        if(dna == -1) {
            this.dna = 120;
        }
        this.random = ThreadLocalRandom.current();
        this.moves = 0;
        this.reproduceConsumption = game.v();
        this.M = game.M();

    }

    @Override
    public String name() {
        return "Smart Plague";
    }

    @Override
    public Color color() {
        return new Color(192, 192, 192, 255);
    }

    @Override
    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW) {

        moves++;
        Action stay = Action.fromInt(0);

        List<Integer> directions = emptyNeighboringCells(neighborN, neighborS, neighborW, neighborE);
        if(directions.isEmpty()) {
            //If we're trapped, we can't move
            return Move.movement(Action.fromInt(0));
        }

        int randomAvailableDirection = directions.get(randomAvailableDirectionIndex(directions));

        //Check if there's food in 4 directions
        List<Integer> directionsWithFood = directionsWithFood(foodN, foodS, foodW, foodE);
        if(!directionsWithFood.isEmpty()) {
            randomAvailableDirection = directionsWithFood.get(randomAvailableDirectionIndex(directionsWithFood));
        }

        //If we have enough energy, spawn a child into a random direction
        if(energyLeft > reproduceThreshold()) {
            Action childPosition = Action.fromInt(randomAvailableDirection);
            return Move.reproduce(childPosition, dna + 10);
        }

        //not zero so that way you can leave and come back for it to double
        if(foodHere > 2) {
            return Move.movement(stay);
        }

        if(!directionsWithFood.isEmpty()) {
            return Move.movement(Action.fromInt(randomAvailableDirection));
        }

        if(moveFrequency()!=0 && moves % moveFrequency() == 0 && moves < 100) { //If we haven't moved in the past moveFreq moves, move
            return Move.movement(Action.fromInt(randomAvailableDirection));
        }

        return Move.movement(Action.fromInt(0));

    }

    // UTILITY FUNCTIONS ----------------------------------------------------------------------------------------

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

    private int moveFrequency() {
        if(dna >= 300) {
            return 0;
        }
        return (int)(2 + 1 * (((dna - 120) * (dna - 120)) / 120f)); //Default
    }

    private int reproduceThreshold() {
        return Math.min(dna, 500);
    }

    @Override
    public int externalState() {
        return 0; //TODO: Consider changing ?
    }
}