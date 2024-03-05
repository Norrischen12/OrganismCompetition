package organisms.g3;

import organisms.Constants;
import organisms.Move;
import organisms.OrganismsPlayer;
import organisms.g1.model.GameSquare;
import organisms.ui.OrganismsGame;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Desert2 {

    private int dna;
    private ThreadLocalRandom random;
    private int energyForStayingPut;
    private int energyForMovingReproducing;
    private int energyPerUnitFood;
    private int maxEnergy;
    private Constants.Action[] tracking;
    private Map<String, GameSquare> infoMap;
    private int currX, currY, numberOfMovesMade, negLimit, posLimit;
    private GameSquare defaultSquare;
    private final int movesToTrack = 50;

    private boolean foodNPrev = false;
    private boolean foodEPrev = false;
    private boolean foodSPrev = false;
    private boolean foodWPrev = false;

    private int neighborNPrev = 0;
    private int neighborEPrev = 0;
    private int neighborSPrev = 0;
    private int neighborWPrev = 0;

    public void register(OrganismsGame game, int dna) throws Exception {
        this.dna = dna;
        this.random = ThreadLocalRandom.current();
        this.dna = dna;
        energyForStayingPut = game.s();
        energyForMovingReproducing = game.v();
        energyPerUnitFood = game.u();
        maxEnergy = game.M();
        this.random = ThreadLocalRandom.current();

        infoMap = new HashMap<>();
        currX = 0;
        currY = 0;
        infoMap.put(currX + "," + currY, new GameSquare());
        infoMap.get(currX + "," + currY).incrementNumberOfTimeVisited();

        numberOfMovesMade = 0;
        tracking = new Constants.Action[100];
        defaultSquare = new GameSquare();

        negLimit = (int) -(movesToTrack * 1.25);
        posLimit = (int) (movesToTrack * 1.25);
    }

    public String name() {
        return "Lantern Fly";
    }

    public Color color() {
        return new Color(31, 81, 255, 255);
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

    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW, int dna, OrganismsGame game, ThreadLocalRandom random) {

        if (this.dna == -1) {
            this.dna = 120;
        }
        ++numberOfMovesMade;
        foodNPrev = foodN;
        foodEPrev = foodE;
        foodSPrev = foodS;
        foodWPrev = foodW;

        neighborNPrev = neighborN;
        neighborEPrev = neighborE;
        neighborSPrev = neighborS;
        neighborWPrev = neighborS;

        Object[] directionAndFood = bestDirection(foodN, foodE, foodS, foodW, neighborN, neighborE, neighborS, neighborW, energyLeft);
        Constants.Action direction = (Constants.Action) directionAndFood[0];
        boolean foodInNeighboursConsideringDirection = (boolean) directionAndFood[1];

        if(numberOfMovesMade > movesToTrack) infoMap.clear();
        infoMap.putIfAbsent(currX + "," + currY, new GameSquare());
        infoMap.get(currX + "," + currY).incrementNumberOfTimeVisited();

        // updates details about neighbours and current cell and updates currX and currY considering the direction decided
        updateInfoMap(direction, foodN, foodE, foodS, foodW, neighborN, neighborE, neighborS, neighborW);
        boolean foodInNeighbours = foodE || foodN || foodS || foodW;
        Move moveToMake;

        this.dna = dna % 100 + 100;
        this.random = random;

        if (this.dna >= 123) {
            if ((this.dna == 123 && foodS) || (this.dna == 124 && foodE) || (this.dna == 125 && foodW) || (this.dna == 126 && foodN)) {
                // check if we are a part of a broken garden, if we are reset dna to 120 and move away
                if (this.dna == 123 && Util.isNeighbor(neighborS) && neighborS == 120) {
                    this.dna = 120;
                    return Move.movement(Constants.Action.NORTH);
                }
                if (this.dna == 124 && Util.isNeighbor(neighborE) && neighborE == 120) {
                    this.dna = 120;
                    return Move.movement(Constants.Action.WEST);
                }
                if (this.dna == 125 && Util.isNeighbor(neighborW) && neighborW == 120) {
                    this.dna = 120;
                    return Move.movement(Constants.Action.EAST);
                }
                if (this.dna == 126 && Util.isNeighbor(neighborN) && neighborN == 120) {
                    this.dna = 120;
                    return Move.movement(Constants.Action.SOUTH);
                }

                if (energyLeft <= game.v() + 10) {
                    if (this.dna == 123) {
                        if (!Util.isNeighbor(neighborS)) {
                            this.dna = 127;
                            return Move.movement(Constants.Action.SOUTH);
                        }
                        return Move.movement(Constants.Action.STAY_PUT);
                    } else if (this.dna == 124) {
                        if (!Util.isNeighbor(neighborE)) {
                            this.dna = 128;
                            return Move.movement(Constants.Action.EAST);
                        }
                        return Move.movement(Constants.Action.STAY_PUT);
                    } else if (this.dna == 125) {
                        if (!Util.isNeighbor(neighborW)) {
                            this.dna = 129;
                            return Move.movement(Constants.Action.WEST);
                        }
                        return Move.movement(Constants.Action.STAY_PUT);
                    } else {
                        if (!Util.isNeighbor(neighborN)) {
                            this.dna = 130;
                            return Move.movement(Constants.Action.NORTH);
                        }
                        return Move.movement(Constants.Action.STAY_PUT);
                    }
                } else {
                    return Move.movement(Constants.Action.STAY_PUT);
                }
            }

            // allows those in gardens to reproduce
            if (this.dna >= 127 && foodHere > 12 && numNeighbors(neighborN, neighborE, neighborS, neighborW) < 4) {
                return Move.reproduce(Util.directToReproduce(neighborN, neighborE, neighborS, neighborW), -1);
            }

            if (this.dna == 127) {
                if (!Util.isNeighbor(neighborN)) {
                    this.dna = 123;
                    return Move.movement(Constants.Action.NORTH);
                }
                return Move.movement(Constants.Action.STAY_PUT);
            } else if (this.dna == 128) {
                if (!Util.isNeighbor(neighborW)) {
                    this.dna = 124;
                    return Move.movement(Constants.Action.WEST);
                }
                return Move.movement(Constants.Action.STAY_PUT);
            } else if (this.dna == 129) {
                if (!Util.isNeighbor(neighborE)) {
                    this.dna = 125;
                    return Move.movement(Constants.Action.EAST);
                }
                return Move.movement(Constants.Action.STAY_PUT);
            } else if (this.dna == 130) {
                if (!Util.isNeighbor(neighborS)) {
                    this.dna = 126;
                    return Move.movement(Constants.Action.SOUTH);
                }
                return Move.movement(Constants.Action.STAY_PUT);
            } else {
                this.dna = 120;
            }
        } else if (this.dna == 120) {
            // determine if we are part of a garden that isn't functional. If it is, become appropriate garden value
            int replacementGarden = getReplacementGarden(neighborN, neighborE, neighborS, neighborW);
            if (replacementGarden != 120) {
                this.dna = replacementGarden;
                return Move.movement(Constants.Action.STAY_PUT);
            }


            // create the garden logic - edit this for non garden behavior
            if (foodHere > 7) {
                if (!Util.isNeighbor(neighborW)) {
                    this.dna = 121;
                    return Move.reproduce(Constants.Action.WEST, 124);
                }
                return Move.movement(Constants.Action.STAY_PUT);
            }

            // if we are not in a garden and not making a garden

            if (direction == Constants.Action.STAY_PUT) {
                moveToMake = Move.movement(Constants.Action.STAY_PUT);
            } else if (isHungry(energyLeft) && foodHere <= 0 && enoughEnergy(energyLeft, foodHere, foodInNeighbours, false)) {
                moveToMake = Move.movement(direction);
            } else if (enoughEnergy(energyLeft, foodHere, foodInNeighbours, true) && foodInNeighboursConsideringDirection) {
                moveToMake = Move.reproduce(direction, dna);
            } else if (foodHere <= 0 && enoughEnergy(energyLeft, foodHere, foodInNeighbours, false)) {
                moveToMake = Move.movement(direction);
            } else {
                moveToMake = Move.movement(Constants.Action.STAY_PUT);
            }

            tracking[numberOfMovesMade % tracking.length] = moveToMake.getAction();

            return moveToMake;

//            if (foodHere >= 1 && numNeighbors(neighborN, neighborE, neighborS, neighborW) < 3) {
//                Constants.Action direc = Util.directToReproduce(neighborN, neighborE, neighborS, neighborW);
//                if (direc != null) {
//                    return Move.reproduce(direc, this.dna);
//                }
//                return Move.movement(Constants.Action.STAY_PUT);
//            }
//            if (numFood(foodN, foodE, foodS, foodW) > 1 && energyLeft > game.v() * 5) {
//                int randomChoice = random.nextInt(3);
//                if (randomChoice == 0) {
//                    Constants.Action direc = Util.directToReproduce(neighborN, neighborE, neighborS, neighborW);
//                    if (direc != null) {
//                        return Move.reproduce(direc, this.dna);
//                    }
//                    return Move.movement(Constants.Action.STAY_PUT);
//                }
//            }
//            Constants.Action moveOntoFood = Util.foodWithoutOpponent(foodN, foodE, foodS, foodW, neighborN, neighborE, neighborS, neighborW);
//            if (moveOntoFood != null) {
//                if (energyLeft <= game.v() + 1 || energyLeft > 320) {
//                    return Move.movement(moveOntoFood);
//                } else {
//                    return Move.movement(Constants.Action.STAY_PUT);
//                }
//            }

            // pick randomly between north and west and staying put,
//            int randomChoice = random.nextInt(22);
//            if (randomChoice == 0) {
//                return Move.movement(Constants.Action.NORTH);
//            } else if (randomChoice == 1) {
//                return Move.movement(Constants.Action.WEST);
//            } else {
//                return Move.movement(Constants.Action.STAY_PUT);
//            }



        // continue spawning the garden logic
        } else if (this.dna == 121) {
            if (!Util.isNeighbor(neighborE)) {
                this.dna = 122;
                return Move.reproduce(Constants.Action.EAST, 125);
            }
            return Move.movement(Constants.Action.STAY_PUT);
        } else if (this.dna == 122) {
            if (!Util.isNeighbor(neighborS)) {
                this.dna = 127;
                return Move.reproduce(Constants.Action.SOUTH, 126);
            }
            return Move.movement(Constants.Action.STAY_PUT);
        }
        return Move.movement(Constants.Action.STAY_PUT);

    }

    public int getDNA() {
        return this.dna;
    }

    public int externalState() throws Exception {
        if (foodNPrev && neighborNPrev != -1) {
            return 1; // 1 means food above
        } else if (foodSPrev && neighborSPrev != -1) {
            return 2; // 2 means food below
        } else if (foodWPrev && neighborWPrev != -1) {
            return 3; // 3 means food to the left
        } else if (foodEPrev && neighborEPrev != -1) {
            return 4; // 4 means food to the right
        }
        return 0;
    }

    private boolean enoughEnergy(int currentEnergy, int foodHere, boolean foodInNeighbours, boolean toReproduce) {
        int v = energyForMovingReproducing;
        int M = maxEnergy;
        if (toReproduce) {
            double newCurrentEnergy = (currentEnergy - v) / 2.0;
            double reproductionThreshold = (foodInNeighbours ? Math.min(M / 4.0, 10 * v) : Math.min(M / 3.0, 10 * v));
            boolean condition1 = newCurrentEnergy > reproductionThreshold;
            boolean condition2 = (foodHere <= 1.5 * v);
            return condition1 && condition2 && foodHere > 0;
        } else {
            return currentEnergy - v > 0;
        }
    }

    private boolean isHungry(int currentEnergy) {
        return currentEnergy < energyPerUnitFood;
    }

    private double getProbabilityOfFoodAroundCurrentSquare(Constants.Action action) {
        return switch (action) {
            case NORTH -> infoMap.getOrDefault(currX + "," + (currY - 1), defaultSquare).getLocalProbabilityOfFood();
            case EAST -> infoMap.getOrDefault((currX + 1) + "," + currY, defaultSquare).getLocalProbabilityOfFood();
            case SOUTH -> infoMap.getOrDefault(currX + "," + (currY + 1), defaultSquare).getLocalProbabilityOfFood();
            case WEST -> infoMap.getOrDefault((currX - 1) + "," + currY, defaultSquare).getLocalProbabilityOfFood();
            default -> 0;
        };
    }

    public double getProbabilityOfFoodAroundNextSquare(Constants.Action action) {
        return switch (action) {
            case NORTH -> totalProbabilityOfFoodInNeighbours(currX, currY - 1);
            case SOUTH -> totalProbabilityOfFoodInNeighbours(currX, currY + 1);
            case EAST -> totalProbabilityOfFoodInNeighbours(currX + 1, currY);
            case WEST -> totalProbabilityOfFoodInNeighbours(currX - 1, currY);
            default -> -1d;
        };
    }

    private double totalProbabilityOfFoodInNeighbours(int x, int y) {
        double total = 0;
        total += infoMap.getOrDefault(x + "," + (y - 1), defaultSquare).getLocalProbabilityOfFood();
        total += infoMap.getOrDefault((x + 1) + "," + y, defaultSquare).getLocalProbabilityOfFood();
        total += infoMap.getOrDefault(x + "," + (y + 1), defaultSquare).getLocalProbabilityOfFood();
        total += infoMap.getOrDefault((x - 1) + "," + y, defaultSquare).getLocalProbabilityOfFood();
        return total;
    }

    private Constants.Action getActionConsideringProbability(java.util.List<Constants.Action> candidates, boolean foodInNeighbour, int currEnergy) {
        // think if we want to include number of players in the decision rather than just food probability
        if (candidates.isEmpty()) return Constants.Action.STAY_PUT;
        if (candidates.size() == 1) return candidates.get(0);

        Map<Constants.Action, Double> probabilities = new HashMap<>();
        for (Constants.Action action : candidates) {
            if (foodInNeighbour)
                probabilities.put(action, getProbabilityOfFoodAroundCurrentSquare(action));
            else
                probabilities.put(action, getProbabilityOfFoodAroundNextSquare(action));
        }
        double maxProbability = currEnergy < 100 ? -1 : 0;
        Constants.Action actionWithMaxProbability = null;
        for (Map.Entry<Constants.Action, Double> entry : probabilities.entrySet()) {
            if (entry.getValue() > maxProbability) {
                maxProbability = entry.getValue();
                actionWithMaxProbability = entry.getKey();
            }
        }
        if (actionWithMaxProbability == null) return Constants.Action.STAY_PUT;
        return actionWithMaxProbability;
    }

    private Object[] bestDirection(boolean foodN, boolean foodE, boolean foodS, boolean foodW,
                                   int neighborN, int neighborE, int neighborS, int neighborW, int currEnergy) {
        List<Constants.Action> candidates = new ArrayList<>();
        if (foodN && neighborN == -1) candidates.add(Constants.Action.NORTH);
        if (foodE && neighborE == -1) candidates.add(Constants.Action.EAST);
        if (foodS && neighborS == -1) candidates.add(Constants.Action.SOUTH);
        if (foodW && neighborW == -1) candidates.add(Constants.Action.WEST);
        if (!candidates.isEmpty())
            return new Object[]{getActionConsideringProbability(candidates, true, currEnergy), true};

        int percentage = 0; // in the case where s >= v
        if (energyForStayingPut <= energyForMovingReproducing / 9) percentage = 50;
        else if (energyForStayingPut <= energyForMovingReproducing / 4) percentage = 25;
        else if (energyForStayingPut <= energyForMovingReproducing / 2) percentage = 10;
        else if (energyForStayingPut < energyForMovingReproducing) percentage = 2;


        if (getBestConsideringSignal(neighborN,  neighborE,  neighborS,  neighborW) != null) {
            return getBestConsideringSignal(neighborN,  neighborE,  neighborS,  neighborW);
        }

        // // give respective weightage to staying put if s <<< v when you don't have food around you
        if (random.nextInt(100) < percentage) return new Object[]{Constants.Action.STAY_PUT, false};


        if (neighborN == -1) candidates.add(Constants.Action.NORTH);
        if (neighborE == -1) candidates.add(Constants.Action.EAST);
        if (neighborS == -1) candidates.add(Constants.Action.SOUTH);
        if (neighborW == -1) candidates.add(Constants.Action.WEST);
        if (!candidates.isEmpty())
            return new Object[]{getActionConsideringProbability(candidates, false, currEnergy), false};

        return new Object[]{Constants.Action.STAY_PUT, false};
    }

    private Object[] getBestConsideringSignal(int neighborN, int neighborE, int neighborS, int neighborW) {
        // neighbor above signal right
        if (neighborN == 4 && neighborE == -1) {
            return new Object[]{Constants.Action.EAST, false};
        }
        // neighbor above signal left
        if (neighborN == 3 && neighborW == -1) {
            return new Object[]{Constants.Action.WEST, false};
        }
        // neighbor below signal right
        if (neighborS == 4 && neighborE == -1) {
            return new Object[]{Constants.Action.EAST, false};
        }
        // neighbor below signal left
        if (neighborS == 3 && neighborW == -1) {
            return new Object[]{Constants.Action.WEST, false};
        }

        // neighbor right signal up
        if (neighborE == 1 && neighborN == -1) {
            return new Object[]{Constants.Action.NORTH, false};
        }
        // neighbor right signal down
        if (neighborE == 2 && neighborS == -1) {
            return new Object[]{Constants.Action.SOUTH, false};
        }

        // neighbor left signal up
        if (neighborW == 1 && neighborN == -1) {
            return new Object[]{Constants.Action.NORTH, false};
        }
        // neighbor right signal down
        if (neighborW == 2 && neighborS == -1) {
            return new Object[]{Constants.Action.SOUTH, false};
        }
        return null;
    }

    private void updateInfoMap(Constants.Action action, boolean foodN, boolean foodE,
                               boolean foodS, boolean foodW, int neighborN, int neighborE,
                               int neighborS, int neighborW) {
        if (currX >= negLimit && currX < posLimit && currY-1 >= negLimit && currY-1 < posLimit) {
            String key = currX + "," + (currY-1);
            infoMap.putIfAbsent(key, new GameSquare());
            infoMap.get(key).incrementNumberOfTimeVisited();
            if (foodN) infoMap.get(key).incrementNumberOfTimeFood();
            if (neighborN != -1) infoMap.get(key).incrementNumberOfTimePlayer();
        }

        if (currX+1 >= negLimit && currX+1 < posLimit && currY >= negLimit && currY < posLimit) {
            String key = (currX+1) + "," + currY;
            infoMap.putIfAbsent(key, new GameSquare());
            infoMap.get(key).incrementNumberOfTimeVisited();
            if (foodE) infoMap.get(key).incrementNumberOfTimeFood();
            if (neighborE != -1) infoMap.get(key).incrementNumberOfTimePlayer();
        }

        if (currX >= negLimit && currX < posLimit && currY+1 >= negLimit && currY+1 < posLimit) {
            String key = currX + "," + (currY+1);
            infoMap.putIfAbsent(key, new GameSquare());
            infoMap.get(key).incrementNumberOfTimeVisited();
            if (foodS) infoMap.get(key).incrementNumberOfTimeFood();
            if (neighborS != -1) infoMap.get(key).incrementNumberOfTimePlayer();
        }

        if (currX-1 >= negLimit && currX-1 < posLimit && currY >= negLimit && currY < posLimit) {
            String key = (currX-1) + "," + currY;
            infoMap.putIfAbsent(key, new GameSquare());
            infoMap.get(key).incrementNumberOfTimeVisited();
            if (foodW) infoMap.get(key).incrementNumberOfTimeFood();
            if (neighborW != -1) infoMap.get(key).incrementNumberOfTimePlayer();
        }

        switch (action) {
            case NORTH -> currY--;
            case SOUTH -> currY++;
            case EAST -> currX++;
            case WEST -> currX--;
        }
    }

}
