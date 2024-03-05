package organisms.g3;

import organisms.Move;
import organisms.ui.OrganismsGame;
import organisms.OrganismsPlayer;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Desert implements OrganismsPlayer {
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
        return "Lantern Fly";
    }

    @Override
    public Color color() {
        return new Color(31, 81, 255, 255);
    }

    double foodEstimation = 0.0;
    double playerEstimation =  0.0;
    int numTurns = 0;
    int numSquares = 0;
    int numPlayers = 0;
    int numFood = 0;
    boolean justMoved = true;

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

    int getReplacementGarden(int neighborN, int neighborE, int neighborS, int neighborW) {
        if (numNeighbors(neighborN, neighborE, neighborS, neighborW) == 3) {
            if (!Util.isNeighbor(neighborN) || neighborN != 123) {
                return 123;
            }
            if (!Util.isNeighbor(neighborW) || neighborW != 124) {
                return 124;
            }
            if (!Util.isNeighbor(neighborE) || neighborE != 125) {
                return 125;
            }
            return 127;
        }
        return 120;
    }

    @Override
    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW) {

        // update DNA
        if (dna == -1) {
            dna = 120;
        }

        if (dna >= 123) {
            if ((dna == 123 && foodS) || (dna == 124 && foodE) || (dna == 125 && foodW) || (dna == 126 && foodN)) {
                // check if we are a part of a broken garden, if we are reset dna to 120 and move away
                if (dna == 123 && Util.isNeighbor(neighborS) && neighborS == 120) {
                    dna = 120;
                    return Move.movement(Action.NORTH);
                }
                if (dna == 124 && Util.isNeighbor(neighborE) && neighborE == 120) {
                    dna = 120;
                    return Move.movement(Action.WEST);
                }
                if (dna == 125 && Util.isNeighbor(neighborW) && neighborW == 120) {
                    dna = 120;
                    return Move.movement(Action.EAST);
                }
                if (dna == 126 && Util.isNeighbor(neighborN) && neighborN == 120) {
                    dna = 120;
                    return Move.movement(Action.SOUTH);
                }

                if (energyLeft <= this.game.v() + 10) {
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
            // determine if we are part of a garden that isn't functional. If it is, become appropriate garden value
            int replacementGarden = getReplacementGarden(neighborN, neighborE, neighborS, neighborW);
            if (replacementGarden != 120) {
                dna = replacementGarden;
                return Move.movement(Action.STAY_PUT);
            }
            if (foodHere > 4) {
                if (!Util.isNeighbor(neighborW)) {
                    dna = 121;
                    return Move.reproduce(Action.WEST, 124);
                }
                return Move.movement(Action.STAY_PUT);
            } else {
                if (foodHere >= 1 && numNeighbors(neighborN, neighborE, neighborS, neighborW) < 3) {
                    Action direc = Util.directToReproduce(neighborN, neighborE, neighborS, neighborW);
                    if (direc != null) {
                        return Move.reproduce(direc, dna);
                    }
                    return Move.movement(Action.STAY_PUT);
                }
                if (numFood(foodN, foodE, foodS, foodW) > 1 && energyLeft > this.game.v() * 5) {
                    int randomChoice = random.nextInt(3);
                    if (randomChoice == 0) {
                        Action direc = Util.directToReproduce(neighborN, neighborE, neighborS, neighborW);
                        if (direc != null) {
                            return Move.reproduce(direc, dna);
                        }
                        return Move.movement(Action.STAY_PUT);
                    }
                }
                Action moveOntoFood = Util.foodWithoutOpponent(foodN, foodE, foodS, foodW, neighborN, neighborE, neighborS, neighborW);
                if (moveOntoFood != null) {
                    if (energyLeft <= this.game.v() + 1 || energyLeft > 320) {
                        return Move.movement(moveOntoFood);
                    } else {
                        return Move.movement(Action.STAY_PUT);
                    }
                }

                // pick randomly between north and west and staying put,
                int randomChoice = random.nextInt(22);
                if (randomChoice == 0) {
                    return Move.movement(Action.NORTH);
                } else if (randomChoice == 1) {
                    return Move.movement(Action.WEST);
                } else {
                    return Move.movement(Action.STAY_PUT);
                }
            }
        } else if (dna == 121) {
            if (!Util.isNeighbor(neighborE)) {
                dna = 122;
                return Move.reproduce(Action.EAST, 125);
            }
            return Move.movement(Action.STAY_PUT);
        } else if (dna == 122) {
            if (!Util.isNeighbor(neighborS)) {
                dna = 127;
                return Move.reproduce(Action.SOUTH, 126);
            }
            return Move.movement(Action.STAY_PUT);
        }
        return Move.movement(Action.STAY_PUT);

    }

    @Override
    public int externalState() {
        return dna;
    }
}
