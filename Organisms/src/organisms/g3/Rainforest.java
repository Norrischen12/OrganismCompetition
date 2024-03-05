package organisms.g3;

import organisms.Move;
import organisms.ui.OrganismsGame;
import organisms.OrganismsPlayer;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Rainforest implements OrganismsPlayer {
    private OrganismsGame game;
    private int dna;
    private ThreadLocalRandom random;
    private int numSpawned = 0;
    private int orig = 0;

    @Override
    public void register(OrganismsGame game, int dna) throws Exception {
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

    Action returnToOrig(int n, int neighborN, int neighborS, int neighborE, int neighborW) {
        if (n == 1 && !Util.isNeighbor(neighborE)) {
            return Action.EAST;
        } else if (n == 2 && !Util.isNeighbor(neighborW)) {
            return Action.WEST;
        } else if (n == 3 && !Util.isNeighbor(neighborS)) {
            return Action.SOUTH;
        } else if (n == 4 && !Util.isNeighbor(neighborN)) {
            return Action.NORTH;
        }
        return Action.STAY_PUT;
    }

    @Override
    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW) {
        if (dna == -1) {
            dna = 5;
            orig = 1;
        }
        // dna = 5 -> on home square
        // dna = 6 -> must move to home square
        // dna = 1 through 4 -> Constants
        if (dna == 5) {
            if (energyLeft < this.game.v() * 4) {
                Action foodChoice = Util.foodWithoutOpponent(foodN, foodE, foodS, foodW, neighborN, neighborE,
                neighborS, neighborW);
                if (foodChoice != null) {
                    if (foodChoice == Action.WEST) {
                        dna = 1;
                    } else if (foodChoice == Action.EAST) {
                        dna = 2;
                    } else if (foodChoice == Action.NORTH) {
                        dna = 3;
                    } else if (foodChoice == Action.SOUTH) {
                        dna = 4;
                    }
                    return Move.movement(foodChoice);
                }
            } else {
                if (!Util.isNeighbor(neighborN) && numSpawned < 2) {
                    numSpawned++;
                    return Move.reproduce(Action.NORTH, 6);
                } else if (orig != 1) {
                    return Move.movement(Action.STAY_PUT);
                }
            }
        } else if (dna == 6) {
            if (!Util.isNeighbor(neighborE) && !Util.isNeighbor(neighborW)) {
                dna = 5;
                int randomChoice = random.nextInt(2);
                if (randomChoice == 0) {
                    return Move.movement(Action.EAST);
                }
                return Move.movement(Action.WEST);
            } else if (!Util.isNeighbor(neighborE)) {
                dna = 5;
                return Move.movement(Action.EAST);
            } else if (!Util.isNeighbor(neighborW)) {
                dna = 5;
                return Move.movement(Action.WEST);
            } else if (!Util.isNeighbor(neighborN)) {
                dna = 5;
                return Move.movement(Action.NORTH);
            }
            return Move.movement(Action.STAY_PUT);
        } else if (dna > 0) {
            Action moveHome = returnToOrig(Math.abs(dna), neighborN, neighborS, neighborE, neighborW);
            if (moveHome != Action.STAY_PUT) {
                dna = 5;
            }
            return Move.movement(moveHome);
        }

        if (dna == -5 || orig == 1) {
            if (energyLeft < this.game.v() * 4) {
                Action foodChoice = Util.foodWithoutOpponent(foodN, foodE, foodS, foodW, neighborN, neighborE,
                        neighborS, neighborW);
                if (foodChoice != null) {
                    if (foodChoice == Action.WEST) {
                        dna = -1;
                    } else if (foodChoice == Action.EAST) {
                        dna = -2;
                    } else if (foodChoice == Action.NORTH) {
                        dna = -3;
                    } else if (foodChoice == Action.SOUTH) {
                        dna = -4;
                    }
                    return Move.movement(foodChoice);
                }
            } else {
                if (!Util.isNeighbor(neighborS) && numSpawned < 2 || (!Util.isNeighbor(neighborS) && numSpawned < 4 && orig == 1)) {
                    numSpawned++;
                    return Move.reproduce(Action.SOUTH, -6);
                }
                return Move.movement(Action.STAY_PUT);
            }
        } else if (dna == -6) {
            if (!Util.isNeighbor(neighborE) && !Util.isNeighbor(neighborW)) {
                dna = -5;
                int randomChoice = random.nextInt(2);
                if (randomChoice == 0) {
                    return Move.movement(Action.EAST);
                }
                return Move.movement(Action.WEST);
            } else if (!Util.isNeighbor(neighborE)) {
                dna = -5;
                return Move.movement(Action.EAST);
            } else if (!Util.isNeighbor(neighborW)) {
                dna = -5;
                return Move.movement(Action.WEST);
            } else if (!Util.isNeighbor(neighborS)) {
                dna = -5;
                return Move.movement(Action.SOUTH);
            }
            return Move.movement(Action.STAY_PUT);
        } else if (dna < 0) {
            Action moveHome = returnToOrig(Math.abs(dna), neighborN, neighborS, neighborE, neighborW);
            if (moveHome != Action.STAY_PUT) {
                dna = -5;
            }
            return Move.movement(moveHome);
        }

        return Move.movement(Action.STAY_PUT);
    }

    @Override
    public int externalState() {
        return dna;
    }
}
