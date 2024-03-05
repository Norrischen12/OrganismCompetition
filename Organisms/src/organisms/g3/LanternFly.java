package organisms.g3;

import organisms.Move;
import organisms.ui.OrganismsGame;
import organisms.OrganismsPlayer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class LanternFly implements OrganismsPlayer {
    private OrganismsGame game;
    private int dna;
    private ThreadLocalRandom random;

    @Override
    public void register(OrganismsGame game, int dna) throws Exception {
        this.game = game;
        this.dna = dna;
        this.random = ThreadLocalRandom.current();
    }

    public void setInfo(OrganismsGame game, int dna) throws Exception {
        this.game = game;
        this.dna = dna;
        this.random = ThreadLocalRandom.current();
    }

    @Override
    public String name() {
        return "Lantern Fly";
    }

    @Override
    public Color color() {
        return new Color(31, 81, 255, 255);
    }


    double foodEstimation = 0;
    double playerEstimation =  0;
    int numTurns = 0;
    int numSquares = 0;

    List<Integer> prev10turnsplayers = Arrays.asList(-1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1);

    List<Integer> prev10turnsfood = Arrays.asList(-1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1);


    int memorySize = 200;
    boolean[][] memory = new boolean[memorySize][memorySize];
    Point currPos = new Point(memorySize/2, memorySize/2);


    // attempt at a function that can approximate percentage of food on turn
    public void updateDNA(int neighborN, int neighborE, int neighborS, int neighborW) {
        ArrayList<Integer> newDNA = new ArrayList<>();
        if (Util.isNeighbor(neighborN)) {
            newDNA.add(neighborN);
        }
        if (Util.isNeighbor(neighborS)) {
            newDNA.add(neighborS);
        }
        if (Util.isNeighbor(neighborE)) {
            newDNA.add(neighborE);
        }
        if (Util.isNeighbor(neighborW)) {
            newDNA.add(neighborW);
        }
        if (!newDNA.isEmpty()) {
            dna = newDNA.get(this.random.nextInt(newDNA.size()));
        }
    }

    public void updateEstimations(int neighborN, int neighborE, int neighborS, int neighborW,
                                  int foodHere, boolean foodN, boolean foodE, boolean foodS,
                                  boolean foodW) {


        numTurns++;
        int currFood = 0;
        int currPlayers = 0;


        // numSquares = numSquares + 5;


        // if we've already been at this spot
        if (memory[currPos.x][currPos.y]) {
            // then do nothing
        } else {
            numSquares += 3;
        }

        if (Util.isNeighbor(neighborN)) {
            currPlayers++;
        }
        if (Util.isNeighbor(neighborS)) {
            currPlayers++;
        }
        if (Util.isNeighbor(neighborE)) {
            currPlayers++;
        }
        if (Util.isNeighbor(neighborW)) {
            currPlayers++;
        }

        if (foodHere > 0) {
            currFood++;
        }

        if (foodN) {
            currFood++;
        }

        if (foodS) {
            currFood++;
        }

        if (foodE) {
            currFood++;
        }

        if (foodW) {
            currFood++;
        }

        // proportion of sample squares covered by food, players
        // use to estimate in this turn what the state of the board is

        this.prev10turnsplayers.remove(0);
        this.prev10turnsplayers.add(currPlayers);
        this.prev10turnsfood.remove(0);
        this.prev10turnsfood.add(currFood);

        // calculate the average of food counts in the past 10 periods
        // average will be up to 5
        double sumFoodEstimations = 0;
        double sumPlayerEstimations = 0;
        int numNegativeTurns = 0;
        for (int j = 0; j < 9; j++) {
            if (this.prev10turnsplayers.get(j) >= 0) {
                sumPlayerEstimations = sumPlayerEstimations + this.prev10turnsplayers.get(j);
            }

            if (this.prev10turnsfood.get(j) >= 0) {
                sumFoodEstimations = sumFoodEstimations + this.prev10turnsfood.get(j);
            }
            numNegativeTurns++;
        }

        // in the previous turns up to the last 10 turns, how many squares out of 5 have food, players?
        this.foodEstimation = sumFoodEstimations / (10 - numNegativeTurns);
        this.playerEstimation = sumPlayerEstimations / (10 - numNegativeTurns);

    }


    @Override
    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW) {




        // update DNA
//        updateDNA(neighborN, neighborE, neighborS, neighborW);
        if (dna <= 0) {
            dna = 120;
        }


        // update estimations
        updateEstimations(neighborN, neighborE, neighborS, neighborW, foodHere, foodN, foodE, foodS, foodW);

        // this respawns us on the condition there are neighboring food pieces
//        if (energyLeft >= (this.game.M() - this.game.u()) && foodHere >= 1) {
//            Action moveToFood = Util.foodWithoutOpponent(foodN, foodE, foodS, foodW,
//            neighborN, neighborE, neighborS, neighborW);
//            if (moveToFood != null) {
//                return Move.reproduce(moveToFood, dna);
//            }
//        }

        // we are going to go extremely greedy and attempt to make boxes around food
        // createBox
        // 120 is 0 spawned (NONE)
        // 121 is 1 spawned (WEST spawned)
        // 122 is 2 spawned (WEST, EAST spawned)
        // 123 is NORTH of food (WEST, EAST, SOUTH spawned, MOVED north)
        // 124 is WEST of food
        // 125 is EAST of food
        // 126 is SOUTH of food

        // 127 is NORTH moved onto food
        // 128 is WEST moved onto food
        // 129 is EAST moved onto food
        // 130 is SOUTH moved onto food


        // tweak these values to affect our heuristic
        int reproduceEnergyThreshold = 15;
        int moveEnergyThreshold = 20;


        if (dna >= 123) {
            if ((dna == 123 && foodS) || (dna == 124 && foodE) || (dna == 125 && foodW) || (dna == 126 && foodN)) {
                // spawns a new child away from food source
                if (energyLeft > this.game.u() + reproduceEnergyThreshold) {
                    if (dna == 123) {
                        return Move.reproduce(Action.NORTH, 120);
                    } else if (dna == 124) {
                        return Move.reproduce(Action.WEST, 120);
                    } else if (dna == 125) {
                        return Move.reproduce(Action.EAST, 120);
                    } else {
                        return Move.reproduce(Action.SOUTH, 120);
                    }
                } else {
                    if (energyLeft <= moveEnergyThreshold) {
                        if (dna == 123) {
                            if (!Util.isNeighbor(neighborS)) {
                                dna = 127;
                                return Move.movement(Action.SOUTH);
                            }
                            return Move.movement(Action.STAY_PUT);
                        } else if (dna == 124) {
                            if (!Util.isNeighbor(neighborE)) {
                                dna = 128;
                                return Move.movement(Action.EAST);
                            }
                            return Move.movement(Action.STAY_PUT);
                        } else if (dna == 125) {
                            if (!Util.isNeighbor(neighborW)) {
                                dna = 129;
                                return Move.movement(Action.WEST);
                            }
                            return Move.movement(Action.STAY_PUT);
                        } else {
                            if (!Util.isNeighbor(neighborN)) {
                                dna = 130;
                                return Move.movement(Action.NORTH);
                            }
                            return Move.movement(Action.STAY_PUT);
                        }
                    } else {
                        return Move.movement(Action.STAY_PUT);
                    }
                }
            } else if (dna == 127) {
                if (!Util.isNeighbor(neighborN)) {
                    dna = 123;
                    return Move.movement(Action.NORTH);
                }
                return Move.movement(Action.STAY_PUT);
            } else if (dna == 128) {
                if (!Util.isNeighbor(neighborW)) {
                    dna = 124;
                    return Move.movement(Action.WEST);
                }
                return Move.movement(Action.STAY_PUT);
            } else if (dna == 129) {
                if (!Util.isNeighbor(neighborE)) {
                    dna = 125;
                    return Move.movement(Action.EAST);
                }
                return Move.movement(Action.STAY_PUT);
            } else if (dna == 130) {
                if (!Util.isNeighbor(neighborS)) {
                    dna = 126;
                    return Move.movement(Action.SOUTH);
                }
                return Move.movement(Action.STAY_PUT);
            } else {
                dna = 120;
            }
        } else if (dna == 120) {
            if (foodHere > 2) {
                dna = 121;
                return Move.reproduce(Action.WEST, 124);
            } else {
                return Move.movement(Util.whichDirectionToMove(foodN, foodE, foodS, foodW, neighborN, neighborE, neighborS, neighborW));
            }
        } else if (dna == 121) {
            dna = 122;
            return Move.reproduce(Action.EAST, 125);
        } else if (dna == 122) {
            dna = 127;
            return Move.reproduce(Action.SOUTH, 126);
        }
        return Move.movement(Action.STAY_PUT);

//        return Move.movement(Action.STAY_PUT);
//        if (foodHere >= 1 && dna == 120 && ) {
//            dna = 121;
//            return Move.reproduce();
//        }
//
//        if ( != null) {
//
//        } else {
//
//        }
//
//        Action movementChoice = Util.whichDirectionToMove(foodN, foodE, foodS, foodW, neighborN, neighborE, neighborS, neighborW);
//        numMovements++;

        // update memory

//        if (movementChoice.intValue() == 1) {
//            currPos.x -= 1;
//        } else if (movementChoice.intValue() == 2) {
//            currPos.x += 1;
//        } else if (movementChoice.intValue() == 3) {
//            currPos.y += 1;
//        } else if (movementChoice.intValue() == 4) {
//            currPos.y -= 1;
//        }
//
//        memory[currPos.x][currPos.y] = true;
//
//        return Move.movement(movementChoice);
    }

    @Override
    public int externalState() {
        return dna;
    }
}
