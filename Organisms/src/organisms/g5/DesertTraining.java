package organisms.g5;

import organisms.Move;
import organisms.ui.OrganismsGame;
import organisms.OrganismsPlayer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.lang.Math;

public class DesertTraining implements OrganismsPlayer {
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
        return "DT Moquito";
    }

    @Override
    public Color color() {
        return new Color(31, 81, 0, 255);
    }

    int numTurns = 0;
    int numSquares = 0;
    int numPlayers = 0;
    int numFood = 0;
    boolean justMoved = true;
    int turns = 0;
    boolean north;

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
    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW) {

        turns++;
        if (turns < 25) return Move.movement(Action.STAY_PUT);

        if (foodHere >= 1) return Move.movement(Action.WEST);

        if (foodE && !Utils.isNeighbor(neighborE) && energyLeft > this.game.M() - this.game.u()) {

            return mosquitoMove(foodHere, energyLeft, foodN, foodE, 
            foodS, foodW, neighborN, neighborE, neighborS, neighborW);
        }

        if (numFood(foodN, foodE, foodS, foodW) > 2 && energyLeft > this.game.v()) {
            int randomChoice = random.nextInt(2);
            if (randomChoice == 0) {
                return Move.reproduce(Action.NORTH, dna);
            } else {
                return Move.reproduce(Action.WEST, dna);
            }
        }

        Action moveOntoFood = Utils.foodWithoutOpponent(foodN, foodE, foodS, foodW, neighborN, neighborE, neighborS, neighborW);
        if (moveOntoFood != null) {
            if (energyLeft <= 31) {
                return Move.movement(moveOntoFood);
            } else {
                return Move.movement(Action.STAY_PUT);
            }
        }

        // pick randomly between north and west and staying put,
        // int randomChoice = random.nextInt(14);
        // if (randomChoice == 0) {
        //     return Move.movement(Action.NORTH);
        // } else if (randomChoice == 1) {
        //     return Move.movement(Action.WEST);
        // } else {

        //     return Move.movement(Action.STAY_PUT);
        // }

        if (Math.random() > 0.33) {
            return Move.movement(Action.STAY_PUT);
        } 
        else if (north) {
            north = !north;
            return Move.movement(Action.NORTH);
        } else {
            north = !north;
            return Move.movement(Action.WEST);
        } 

    }

    public Move enthusiasmMove(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW) {

        int M = this.game.M();
        int u = this.game.u();

        double reproductionThreshold = M * 0.3;

        //reproduce if there is food on the cell and current energy levels are sufficiently high
        if (foodHere > 0) {
            if (energyLeft > reproductionThreshold) {
                //find best direction to reproduce in
                int bestDir = Utils.simpleFindBestMove(foodN, foodE, foodS, foodW, 
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
            int bestDir = Utils.simpleFindBestMove(foodN, foodE, foodS, foodW, 
                                neighborN, neighborE, neighborS, neighborW);
            if (bestDir != 5) {
                return Move.movement(Action.fromInt(bestDir));
            } 
            //if all adjacent cells are blocked stay put
            return Move.movement(Action.STAY_PUT);
        }
        
    }

    public Move mosquitoMove(int foodHere, int energyLeft, boolean foodN, boolean foodE,
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


    @Override
    public int externalState() {
        return dna;
    }
}
