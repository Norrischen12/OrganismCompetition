package organisms.g5;

import organisms.Move;
import organisms.OrganismsPlayer;
import organisms.g3.Util;
import organisms.ui.OrganismsGame;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Mosquitoes implements OrganismsPlayer {
    private OrganismsGame game;
    private int dna;
    private ThreadLocalRandom random;

    int desertCounter = 0;


    @Override
    public void register(OrganismsGame game, int dna) throws Exception {
        this.game = game;
        this.dna = dna;
        this.random = ThreadLocalRandom.current();
    }

    @Override
    public String name() {
        return "Mosquitoes";
    }

    @Override
    public Color color() {
        return new Color(100, 170, 100, 255);
    }

    @Override
    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW) {

        int M = this.game.M();
        int u = this.game.u();
        int v = this.game.v();
        int K = this.game.K();
        int s = this.game.s();


        double reproduceThreshold;
        boolean detectLocalDesert = false;

        int numNeighbors = findNumNeighbors(neighborN, neighborE, neighborS, neighborW);

        if (numNeighbors < 2 &&  numNeighbors <= this.dna ){
            detectLocalDesert = true;
//            desertCounter++;
            this.dna = numNeighbors;
        }

        if (!detectLocalDesert) {
            reproduceThreshold = 0.6 * M;

            Action stay = Action.fromInt(0);
            //not zero so that way you can leave and come back for it to double
            if (foodHere > 2 * u) {
                return Move.movement(stay);
            }
            java.util.List<Integer> directions = new ArrayList<>();
            if (neighborN == -1) {
                directions.add(3);
            }
            if (neighborS == -1) {
                directions.add(4);
            }
            if (neighborW == -1) {
                directions.add(1);
            }
            if (neighborE == -1) {
                directions.add(2);
            }
            if (directions.isEmpty()) {

                return Move.movement(Action.fromInt(0));
            }
            int randomAvailableDirectionIndex = this.random.nextInt(directions.size());
            int randomAvailableDirection = directions.get(randomAvailableDirectionIndex);

            if (energyLeft > reproduceThreshold) {
                Action childPosition = Action.fromInt(randomAvailableDirection);
                int childKey = this.random.nextInt();
                return Move.reproduce(childPosition, childKey);
            }

            List<Integer> directionsWithFood = new ArrayList<>();
            if (foodN) {
                directionsWithFood.add(3);
            }
            if (foodS) {
                directionsWithFood.add(4);
            }
            if (foodW) {
                directionsWithFood.add(1);
            }
            if (foodE) {
                directionsWithFood.add(2);
            }

            if (!directionsWithFood.isEmpty()) {
                randomAvailableDirectionIndex = this.random.nextInt(directionsWithFood.size());
                randomAvailableDirection = directionsWithFood.get(randomAvailableDirectionIndex);
                return Move.movement(Action.fromInt(randomAvailableDirection));
            }

            return Move.movement(Action.fromInt(0));
        }
        else{
            // if there is food on your square, move one left, wait there
            reproduceThreshold = v; // v
            if (foodHere > 0) {
                return Move.movement(Action.WEST);
            }
            if (foodE && neighborE != -1 && energyLeft > M - u) {
                return Move.reproduce(Action.WEST, dna);
            }
            if (numFood(foodN, foodE, foodS, foodW) > 2 && energyLeft > reproduceThreshold) {
                int randomChoice = random.nextInt(2);
                if (randomChoice == 0) {
                    return Move.reproduce(Action.NORTH, dna);
                } else {
                    return Move.reproduce(Action.WEST, dna);
                }
            }

            Action moveOntoFood = Utils.foodWithoutOpponent(foodN, foodE, foodS, foodW, neighborN, neighborE, neighborS, neighborW);
            if (moveOntoFood != null) {
                if (energyLeft <= M/20) {
                    return Move.movement(moveOntoFood);
                } else {
                    return Move.movement(Action.STAY_PUT);
                }
            }

            // pick randomly between north and west and staying put,
            int randomChoice = random.nextInt(14);
            if (randomChoice == 0) {
                return Move.movement(Action.NORTH);
            } else if (randomChoice == 1) {
                return Move.movement(Action.WEST);
            } else {

                return Move.movement(Action.STAY_PUT);
            }
        }
//        return Move.movement(Action.fromInt(0));
    }

    public int findNumNeighbors(int neighborN, int neighborE, int neighborS, int neighborW){
        int count = 0;

        if(neighborN == -1) {
            count++;
        }
        if(neighborS == -1) {
            count++;
        }
        if(neighborW == -1) {
            count++;
        }
        if(neighborE == -1) {
            count++;
        }
        return count;
    }

    int numFood(boolean foodN, boolean foodE, boolean foodS, boolean foodW) {
        int count = 0;
        if (foodN) {
            count++;
        }
        if (foodE) {
            count++;
        }
        if (foodS) {
            count++;
        }
        if (foodW) {
            count++;
        }
        return count;
    }

    @Override
    public int externalState() {
        return 99;
    }
}
