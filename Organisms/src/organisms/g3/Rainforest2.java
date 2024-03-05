package organisms.g3;

import organisms.Constants;
import organisms.Move;
import organisms.OrganismsPlayer;
import organisms.ui.OrganismsGame;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

public class Rainforest2 {
    private OrganismsGame game;
    private int dna;
    private ThreadLocalRandom random;
    private int numSpawned = 0;
    private int orig = 0;

    public void register(OrganismsGame game, int dna) throws Exception {
        this.game = game;
        this.dna = dna;
        this.random = ThreadLocalRandom.current();
    }

    public String name() {
        return "g3_rainforest";
    }

    public Color color() {
        return new Color(31, 81, 255, 255);
    }

    Constants.Action returnToOrig(int n, int neighborN, int neighborS, int neighborE, int neighborW) {
        if (n == 1 && !Util.isNeighbor(neighborE)) {
            return Constants.Action.EAST;
        } else if (n == 2 && !Util.isNeighbor(neighborW)) {
            return Constants.Action.WEST;
        } else if (n == 3 && !Util.isNeighbor(neighborS)) {
            return Constants.Action.SOUTH;
        } else if (n == 4 && !Util.isNeighbor(neighborN)) {
            return Constants.Action.NORTH;
        }
        return Constants.Action.STAY_PUT;
    }
    int numNeighbors(int neighborN, int neighborE, int neighborS, int neighborW) {
        int count = 0;
        if (Util.isNeighbor(neighborN)) {
            count++;
        }
        if (Util.isNeighbor(neighborE)) {
            count++;
        }
        if (Util.isNeighbor(neighborS)) {
            count++;
        }
        if (Util.isNeighbor(neighborW)) {
            count++;
        }
        return count;
    }

    int recalcNumSpawned(int n, int neighborN, int neighborS, int neighborE, int neighborW) {
        int randomChoice = random.nextInt(6);
        int output = this.numSpawned;
        if (randomChoice == 0) {
            output--;
            return Math.max(0, output);
        }
        if (!Util.isNeighbor(neighborN) && !Util.isNeighbor(neighborE) && !Util.isNeighbor(neighborS) && !Util.isNeighbor(neighborW)) {
            return -1;
        }
        if (n > 0) {
            if ((n == 1 || n == 2) && !Util.isNeighbor(neighborN)) {
                return this.numSpawned - 1;
            } else if (n == 3) {
                output = this.numSpawned;
                if (!Util.isNeighbor(neighborW)) {
                    output--;
                }
                if (!Util.isNeighbor(neighborE)) {
                    output--;
                }
                return Math.max(0, output);
            } else if (n == 4) {
                return this.numSpawned;
            }
            return this.numSpawned;
        } else {
            n = Math.abs(n);
            if ((n == 1 || n == 2) && !Util.isNeighbor(neighborS)) {
                return this.numSpawned - 1;
            } else if (n == 3) {
                return this.numSpawned;
            } else if (n == 4) {
                output = this.numSpawned;
                if (!Util.isNeighbor(neighborW)) {
                    output--;
                }
                if (!Util.isNeighbor(neighborE)) {
                    output--;
                }
                return Math.max(0, output);
            }
            return this.numSpawned;
        }

    }

    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW, int dna, OrganismsGame game, ThreadLocalRandom random) {

        this.dna = dna % 100;
        this.random = random;

        if (this.dna == -1) {
            this.dna = 5;
            orig = 1;
        }
        // dna = 5 -> on home square
        // dna = 6 -> must move to home square
        // dna = 1 through 4 -> Constants
        if (foodHere > 60) {
            this.dna = -1;
            numSpawned = 0;
            if (numNeighbors(neighborN, neighborE, neighborS, neighborW) != 4) {
                return Move.reproduce(Util.directToReproduce(neighborN, neighborE, neighborS, neighborW), -1);
            }
        }
        if (this.dna == 5) {
            if (energyLeft < game.v() * 3) {
                Constants.Action foodChoice = Util.foodWithoutOpponent(foodN, foodE, foodS, foodW, neighborN, neighborE,
                neighborS, neighborW);
                if (foodChoice != null) {
                    if (foodChoice == Constants.Action.WEST) {
                        this.dna = 1;
                    } else if (foodChoice == Constants.Action.EAST) {
                        this.dna = 2;
                    } else if (foodChoice == Constants.Action.NORTH) {
                        this.dna = 3;
                    } else if (foodChoice == Constants.Action.SOUTH) {
                        this.dna = 4;
                    }
                    return Move.movement(foodChoice);
                }
            } else {
//                int numNeighbors = numNeighbors(neighborN, neighborE, neighborS, neighborW);
//                if (numNeighbors == 1 || numNeighbors == 2) {
//                    int randomChoice = random.nextInt(2);
//                    if (randomChoice == 0) {
//                        return Move.reproduce(Constants.Action.EAST, -1);
//                    } else {
//                        return Move.reproduce(Constants.Action.WEST, -1);
//                    }
//                }
                if (!Util.isNeighbor(neighborN) && numSpawned < 2) {
                    numSpawned++;
                    return Move.reproduce(Constants.Action.NORTH, 6);
                } else if (orig != 1) {
                    return Move.movement(Constants.Action.STAY_PUT);
                }
            }
        } else if (this.dna == 6) {
            if (!Util.isNeighbor(neighborE) && !Util.isNeighbor(neighborW)) {
                this.dna = 5;
                int randomChoice = random.nextInt(2);
                if (randomChoice == 0) {
                    return Move.movement(Constants.Action.EAST);
                }
                return Move.movement(Constants.Action.WEST);
            } else if (!Util.isNeighbor(neighborE)) {
                this.dna = 5;
                return Move.movement(Constants.Action.EAST);
            } else if (!Util.isNeighbor(neighborW)) {
                this.dna = 5;
                return Move.movement(Constants.Action.WEST);
            } else if (!Util.isNeighbor(neighborN)) {
                this.dna = 5;
                return Move.movement(Constants.Action.NORTH);
            }
            return Move.movement(Constants.Action.STAY_PUT);
        } else if (this.dna > 0) {
            Constants.Action moveHome = returnToOrig(this.dna, neighborN, neighborS, neighborE, neighborW);
            if (moveHome != Constants.Action.STAY_PUT) {
                this.numSpawned = recalcNumSpawned(Math.abs(this.dna), neighborN, neighborS, neighborE, neighborW);
                if (this.numSpawned == -1) {
                    this.numSpawned = 0;
                    return Move.reproduce(Util.directToReproduce(neighborN, neighborE, neighborS, neighborW), -1);
                }
                this.dna = 5;
            }
            return Move.movement(moveHome);
        }

        if (this.dna == -5 || orig == 1) {
            if (energyLeft < game.v() * 4) {
                Constants.Action foodChoice = Util.foodWithoutOpponent(foodN, foodE, foodS, foodW, neighborN, neighborE,
                        neighborS, neighborW);
                if (foodChoice != null) {
                    if (foodChoice == Constants.Action.WEST) {
                        this.dna = -1;
                    } else if (foodChoice == Constants.Action.EAST) {
                        this.dna = -2;
                    } else if (foodChoice == Constants.Action.NORTH) {
                        this.dna = -3;
                    } else if (foodChoice == Constants.Action.SOUTH) {
                        this.dna = -4;
                    }
                    return Move.movement(foodChoice);
                }
            } else {
//                int numNeighbors = numNeighbors(neighborN, neighborE, neighborS, neighborW);
//                if (numNeighbors == 1 || numNeighbors == 2) {
//                    int randomChoice = random.nextInt(2);
//                    if (randomChoice == 0) {
//                        return Move.reproduce(Constants.Action.EAST, -1);
//                    } else {
//                        return Move.reproduce(Constants.Action.WEST, -1);
//                    }
//                }
                if (!Util.isNeighbor(neighborS) && numSpawned < 2 || (!Util.isNeighbor(neighborS) && numSpawned < 4 && orig == 1)) {
                    numSpawned++;
                    return Move.reproduce(Constants.Action.SOUTH, -6);
                }
                return Move.movement(Constants.Action.STAY_PUT);
            }
        } else if (this.dna == -6) {
            if (!Util.isNeighbor(neighborE) && !Util.isNeighbor(neighborW)) {
                this.dna = -5;
                int randomChoice = random.nextInt(2);
                if (randomChoice == 0) {
                    return Move.movement(Constants.Action.EAST);
                }
                return Move.movement(Constants.Action.WEST);
            } else if (!Util.isNeighbor(neighborE)) {
                this.dna = -5;
                return Move.movement(Constants.Action.EAST);
            } else if (!Util.isNeighbor(neighborW)) {
                this.dna = -5;
                return Move.movement(Constants.Action.WEST);
            } else if (!Util.isNeighbor(neighborS)) {
                this.dna = -5;
                return Move.movement(Constants.Action.SOUTH);
            }
            return Move.movement(Constants.Action.STAY_PUT);
        } else if (this.dna < 0) {
            Constants.Action moveHome = returnToOrig(Math.abs(this.dna), neighborN, neighborS, neighborE, neighborW);
            if (moveHome != Constants.Action.STAY_PUT) {
                this.numSpawned = recalcNumSpawned(Math.abs(this.dna), neighborN, neighborS, neighborE, neighborW);
                if (this.numSpawned == -1) {
                    this.numSpawned = 0;
                    return Move.reproduce(Util.directToReproduce(neighborN, neighborE, neighborS, neighborW), -1);
                }
                this.dna = -5;
            }
            return Move.movement(moveHome);
        }

        return Move.movement(Constants.Action.STAY_PUT);
    }

    public int getDNA() {
        return this.dna;
    }

    public int externalState() {
        return dna;
    }
}
