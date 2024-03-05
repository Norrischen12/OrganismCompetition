package organisms.g1;

import organisms.Constants;
import organisms.Move;
import organisms.OrganismsPlayer;
import organisms.g1.model.GameSquare;
import organisms.g4.Memory;
import organisms.ui.OrganismsGame;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Rainforest implements OrganismsPlayer {
    private int dna;
    private int energyForStayingPut;
    private int energyForMovingReproducing;
    private int energyPerUnitFood;
    private int maxEnergy;
    private ThreadLocalRandom random;
    private Action[] tracking;

    private Map<String, GameSquare> infoMap;
    private int currX, currY, numberOfMovesMade, negLimit, posLimit, numberOfReproductions;

    private GameSquare defaultSquare;
    private final int movesToTrack = 50;

    private boolean isDesparate = false;

    private boolean movedPrev = false;
    private boolean foodNPrev = false;
    private boolean foodEPrev = false;
    private boolean foodSPrev = false;
    private boolean foodWPrev = false;

    private int neighborNPrev = 0;
    private int neighborEPrev = 0;
    private int neighborSPrev = 0;

    private int neighborWPrev = 0;
    private int squaresSeen = 4;
    private int foodSeen = 0;
    private float p;

    @Override
    public void register(OrganismsGame game, int dna) throws Exception {
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
        tracking = new Action[100];
        defaultSquare = new GameSquare();

        negLimit = (int) -(movesToTrack * 1.25);
        posLimit = (int) (movesToTrack * 1.25);
    }

    @Override
    public String name() {
        return "Group 1 Rainforest";
    }

    @Override
    public Color color() {
        return new Color(64, 224, 208, 255);
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

    private double getProbabilityOfFoodAroundCurrentSquare(Action action) {
        return switch (action) {
            case NORTH -> infoMap.getOrDefault(currX + "," + (currY - 1), defaultSquare).getLocalProbabilityOfFood(isDesparate);
            case EAST -> infoMap.getOrDefault((currX + 1) + "," + currY, defaultSquare).getLocalProbabilityOfFood(isDesparate);
            case SOUTH -> infoMap.getOrDefault(currX + "," + (currY + 1), defaultSquare).getLocalProbabilityOfFood(isDesparate);
            case WEST -> infoMap.getOrDefault((currX - 1) + "," + currY, defaultSquare).getLocalProbabilityOfFood(isDesparate);
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
        total += infoMap.getOrDefault(x + "," + (y - 1), defaultSquare).getLocalProbabilityOfFood(isDesparate);
        total += infoMap.getOrDefault((x + 1) + "," + y, defaultSquare).getLocalProbabilityOfFood(isDesparate);
        total += infoMap.getOrDefault(x + "," + (y + 1), defaultSquare).getLocalProbabilityOfFood(isDesparate);
        total += infoMap.getOrDefault((x - 1) + "," + y, defaultSquare).getLocalProbabilityOfFood(isDesparate);
        return total;
    }

    private Action getActionConsideringProbability(List<Action> candidates, boolean foodInNeighbour, int currEnergy) {
        // think if we want to include number of players in the decision rather than just food probability
        if (candidates.isEmpty()) return Action.STAY_PUT;
        if (candidates.size() == 1) return candidates.get(0);

        Map<Action, Double> probabilities = new HashMap<>();
        for (Action action : candidates) {
            if (foodInNeighbour)
                probabilities.put(action, getProbabilityOfFoodAroundCurrentSquare(action));
            else
                probabilities.put(action, getProbabilityOfFoodAroundNextSquare(action));
        }
        double maxProbability = currEnergy < 100 ? -1 : 0;
        Action actionWithMaxProbability = null;
        for (Map.Entry<Action, Double> entry : probabilities.entrySet()) {
            if (entry.getValue() > maxProbability) {
                maxProbability = entry.getValue();
                actionWithMaxProbability = entry.getKey();
            }
        }
        if (actionWithMaxProbability == null) return Action.STAY_PUT;
        return actionWithMaxProbability;
    }

    private Object[] bestDirection(boolean foodN, boolean foodE, boolean foodS, boolean foodW,
                                   int neighborN, int neighborE, int neighborS, int neighborW, int currEnergy) {
        List<Action> candidates = new ArrayList<>();
        if (foodN && neighborN == -1) candidates.add(Action.NORTH);
        if (foodE && neighborE == -1) candidates.add(Action.EAST);
        if (foodS && neighborS == -1) candidates.add(Action.SOUTH);
        if (foodW && neighborW == -1) candidates.add(Action.WEST);
        if (!candidates.isEmpty())
            return new Object[]{getActionConsideringProbability(candidates, true, currEnergy), true};

        int percentage = 0; // in the case where s >= v
        if (energyForStayingPut <= energyForMovingReproducing / 5) percentage = 50;
        else if (energyForStayingPut <= energyForMovingReproducing / 3) percentage = 25;
        else if (energyForStayingPut <= energyForMovingReproducing / 2) percentage = 10;
        else if (energyForStayingPut < energyForMovingReproducing) percentage = 2;


        if (getBestConsideringSignal(neighborN,  neighborE,  neighborS,  neighborW) != null) {
            return getBestConsideringSignal(neighborN,  neighborE,  neighborS,  neighborW);
        }

        // // give respective weightage to staying put if s <<< v when you don't have food around you
        if (random.nextInt(100) < percentage) return new Object[]{Action.STAY_PUT, false};


        if (neighborN == -1) candidates.add(Action.NORTH);
        if (neighborE == -1) candidates.add(Action.EAST);
        if (neighborS == -1) candidates.add(Action.SOUTH);
        if (neighborW == -1) candidates.add(Action.WEST);
        if (!candidates.isEmpty())
            return new Object[]{getActionConsideringProbability(candidates, false, currEnergy), false};

        return new Object[]{Action.STAY_PUT, false};
    }

    private Object[] getBestConsideringSignal(int neighborN, int neighborE, int neighborS, int neighborW) {
        // neighbor above signal right
        if (neighborN == 4 && neighborE == -1) {
            return new Object[]{Action.EAST, false};
        }
        // neighbor above signal left
        if (neighborN == 3 && neighborW == -1) {
            return new Object[]{Action.WEST, false};
        }
        // neighbor below signal right
        if (neighborS == 4 && neighborE == -1) {
            return new Object[]{Action.EAST, false};
        }
        // neighbor below signal left
        if (neighborS == 3 && neighborW == -1) {
            return new Object[]{Action.WEST, false};
        }

        // neighbor right signal up
        if (neighborE == 1 && neighborN == -1) {
            return new Object[]{Action.NORTH, false};
        }
        // neighbor right signal down
        if (neighborE == 2 && neighborS == -1) {
            return new Object[]{Action.SOUTH, false};
        }

        // neighbor left signal up
        if (neighborW == 1 && neighborN == -1) {
            return new Object[]{Action.NORTH, false};
        }
        // neighbor right signal down
        if (neighborW == 2 && neighborS == -1) {
            return new Object[]{Action.SOUTH, false};
        }
        return null;
    }

    private void updateInfoMap(Action action, boolean foodN, boolean foodE,
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

    @Override
    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW) throws Exception {
        if (dna == 55) {
            return moveMemory(foodHere, energyLeft, foodN, foodE, foodS, foodW, neighborN, neighborE, neighborS, neighborW);
        } else {
            return moveUs(foodHere, energyLeft, foodN, foodE, foodS, foodW, neighborN, neighborE, neighborS, neighborW);
        }
    }

    public Move moveUs(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                       boolean foodS, boolean foodW, int neighborN, int neighborE,
                       int neighborS, int neighborW) throws Exception {
        ++numberOfMovesMade;
        foodNPrev = foodN;
        foodEPrev = foodE;
        foodSPrev = foodS;
        foodWPrev = foodW;

        neighborNPrev = neighborN;
        neighborEPrev = neighborE;
        neighborSPrev = neighborS;
        neighborWPrev = neighborS;

        isDesparate = energyLeft < 100;

        Object[] directionAndFood = bestDirection(foodN, foodE, foodS, foodW, neighborN, neighborE, neighborS, neighborW, energyLeft);
        Action direction = (Action) directionAndFood[0];
        boolean foodInNeighboursConsideringDirection = (boolean) directionAndFood[1];

        if(numberOfMovesMade > movesToTrack) infoMap.clear();
        infoMap.putIfAbsent(currX + "," + currY, new GameSquare());
        infoMap.get(currX + "," + currY).incrementNumberOfTimeVisited();

        // updates details about neighbours and current cell and updates currX and currY considering the direction decided
        updateInfoMap(direction, foodN, foodE, foodS, foodW, neighborN, neighborE, neighborS, neighborW);
        boolean foodInNeighbours = foodE || foodN || foodS || foodW;
        Move moveToMake;

        if (direction == Action.STAY_PUT) {
            moveToMake = Move.movement(Action.STAY_PUT);
            movedPrev = false;
        } else if (isHungry(energyLeft) && foodHere <= 0 && enoughEnergy(energyLeft, foodHere, foodInNeighbours, false)) {
            moveToMake = Move.movement(direction);
            movedPrev = true;
        } else if (enoughEnergy(energyLeft, foodHere, foodInNeighbours, true)) {
            /*
            if (numberOfReproductions % 1 == 0) {

            } else {
                moveToMake = Move.reproduce(direction, dna);
            }

             */
            moveToMake = Move.reproduce(direction, 55);
            numberOfReproductions++;
            movedPrev = false;
        } else if (foodHere <= 0 && enoughEnergy(energyLeft, foodHere, foodInNeighbours, false)) {
            moveToMake = Move.movement(direction);
            movedPrev = true;
        } else {
            moveToMake = Move.movement(Action.STAY_PUT);
            movedPrev = false;
        }

        tracking[numberOfMovesMade % tracking.length] = moveToMake.getAction();

        return moveToMake;
    }

    @Override
    public int externalState() throws Exception {
        if (movedPrev) {
            return 0;
        }
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

    public static Constants.Action foodMove(boolean foodN, boolean foodE,
                                            boolean foodS, boolean foodW,
                                            int neighborN, int neighborE,
                                            int neighborS, int neighborW) {
        ArrayList<Constants.Action> potentialMoves = new ArrayList<>();

        if (foodE && neighborE == -1) {
            potentialMoves.add(Constants.Action.EAST);
        }

        if (foodN && neighborN == -1) {
            potentialMoves.add(Constants.Action.NORTH);
        }

        if (foodS && neighborS == -1) {
            potentialMoves.add(Constants.Action.SOUTH);
        }

        if (foodW && neighborW == -1) {
            potentialMoves.add(Constants.Action.WEST);
        }

        // If we have potential moves, select one at random
        if (!potentialMoves.isEmpty()) {
            Collections.shuffle(potentialMoves);
            return potentialMoves.get(0);
        }

        return Constants.Action.STAY_PUT;
    }

    public static Constants.Action randomMove(int neighborN, int neighborE,
                                              int neighborS, int neighborW) {
        ArrayList<Constants.Action> potentialMoves = new ArrayList<>();

        if (neighborE == -1) {
            potentialMoves.add(Constants.Action.EAST);
        }
        if (neighborN == -1) {
            potentialMoves.add(Constants.Action.NORTH);
        }
        if (neighborS == -1) {
            potentialMoves.add(Constants.Action.SOUTH);
        }
        if (neighborW == -1) {
            potentialMoves.add(Constants.Action.WEST);
        }
        // If we have potential moves, select one at random
        if (!potentialMoves.isEmpty()) {
            Collections.shuffle(potentialMoves);
            return potentialMoves.get(0);
        }
        return Constants.Action.STAY_PUT;
    }

    private ArrayList<Constants.Action> neighbors(int neighborN, int neighborE, int neighborS, int neighborW) {
        ArrayList<Constants.Action> directions = new ArrayList<>();

        if (neighborN != -1) {
            directions.add(Constants.Action.NORTH);
        }
        if (neighborE != -1) {
            directions.add(Constants.Action.EAST);
        }
        if (neighborS != -1) {
            directions.add(Constants.Action.SOUTH);
        }
        if (neighborW != -1) {
            directions.add(Constants.Action.WEST);
        }

        return directions;
    }

    private void updateFoodMemory(boolean foodN, boolean foodE,
                                  boolean foodS, boolean foodW, int neighborN, int neighborE,
                                  int neighborS, int neighborW) {
        if (foodN && neighborN == -1) foodMemory.push(Constants.Action.NORTH);
        if (foodE && neighborE == -1) foodMemory.push(Constants.Action.EAST);
        if (foodS && neighborS == -1) foodMemory.push(Constants.Action.SOUTH);
        if (foodW && neighborW == -1) foodMemory.push(Constants.Action.WEST);
    }

    private void updateP(boolean foodN, boolean foodE,
                         boolean foodS, boolean foodW, int neighborN, int neighborE,
                         int neighborS, int neighborW) {
        if (neighborN == -1) {
            squaresSeen += 1;
            if (foodN) {
                foodSeen += 1;
            }
        }
        if (neighborS == -1){
            squaresSeen += 1;
            if (foodS) {
                foodSeen += 1;
            }
        }
        if (neighborE == -1){
            squaresSeen += 1;
            if (foodE) {
                foodSeen += 1;
            }
        }
        if (neighborW == -1) {
            squaresSeen += 1;
            if (foodW) {
                foodSeen += 1;
            }
        }

        p = ((float)foodSeen/(float)squaresSeen);

    }

    private Constants.Action reverseMove(Constants.Action move) {
        switch (move) {
            case NORTH:
                return Constants.Action.SOUTH;
            case SOUTH:
                return Constants.Action.NORTH;
            case EAST:
                return Constants.Action.WEST;
            case WEST:
                return Constants.Action.EAST;
            default:
                return Constants.Action.STAY_PUT;
        }
    }

    private Constants.Action[] trackingMoves = new Constants.Action[10];
    private int moveIndex = 0;
    private int totalMovesMade = 0;

    Stack<Action> foodMemory = new Stack<>();
    enum State {EXPLORING, GOING_TO_FOOD, RETURNING}
    State currentState = State.EXPLORING;
    Constants.Action lastMove = Constants.Action.STAY_PUT;

    public Move moveMemory(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                           boolean foodS, boolean foodW, int neighborN, int neighborE,
                           int neighborS, int neighborW) {
        totalMovesMade++;
        updateP(foodN, foodE, foodS, foodW, neighborN, neighborE, neighborS, neighborW);

        Move finalMove = Move.movement(Constants.Action.STAY_PUT); // Default move.
        Constants.Action moveDir = Constants.Action.STAY_PUT;
        ArrayList<Constants.Action> neighs = neighbors(neighborN, neighborE, neighborS, neighborW);

        boolean hungry = foodHere < 1 && energyLeft >= energyForMovingReproducing;
        boolean surroundedByFood = foodN || foodE || foodS || foodW;
        double reproduceThreshold = surroundedByFood ? (1-p)*((maxEnergy / (energyForMovingReproducing * 0.25))) : (1-p)*(maxEnergy / (energyForMovingReproducing * 0.16));
        boolean reproduce = energyLeft > reproduceThreshold && energyLeft >= energyForMovingReproducing;
        int availableFoodCount = (foodN && neighborN == -1 ? 1 : 0) +
                (foodE && neighborE == -1 ? 1 : 0) +
                (foodS && neighborS == -1 ? 1 : 0) +
                (foodW && neighborW == -1 ? 1 : 0);

        if (foodHere > 0) {  // Stay if there's food
            finalMove = Move.movement(Constants.Action.STAY_PUT);
        }

        switch (currentState) {
            case GOING_TO_FOOD:
                if (!foodMemory.isEmpty()) {
                    lastMove = foodMemory.pop();
                    finalMove = Move.movement(lastMove);
                    if (foodMemory.isEmpty()) {
                        currentState = State.EXPLORING;  // If no more stored moves, explore
                    } else {
                        currentState = State.RETURNING;  // If there are more stored moves, prepare to return after this move
                    }
                }
                break;

            case RETURNING:
                finalMove = Move.movement(reverseMove(lastMove));
                if (foodMemory.isEmpty()) {
                    currentState = State.EXPLORING;
                } else {
                    currentState = State.GOING_TO_FOOD;
                }
                break;

            case EXPLORING:
            default:
                if (reproduce) {
                    int childKey = this.random.nextInt();
                    // Reproduce to the cell with food, if not available reduce to an empty cells
                    moveDir = foodMove(foodN, foodE, foodS, foodW, neighborN, neighborE, neighborS, neighborW);
                    if (moveDir == Constants.Action.STAY_PUT) {
                        moveDir = randomMove(neighborN, neighborE, neighborS, neighborW);
                    }
                    if (moveDir != Action.STAY_PUT) {
                        finalMove = Move.reproduce(moveDir, childKey);
                    }
                }

                if (availableFoodCount > 1) {  // If more than 1 food source is available, update memory and start eating
                    updateFoodMemory(foodN, foodE, foodS, foodW, neighborN, neighborE, neighborS, neighborW);
                    currentState = State.GOING_TO_FOOD;
                } else if (availableFoodCount == 1 && hungry && finalMove.getAction() == Constants.Action.STAY_PUT) {  // If only 1 food source, go there then continue exploring
                    moveDir = foodMove(foodN, foodE, foodS, foodW, neighborN, neighborE, neighborS, neighborW);
                    finalMove = Move.movement(moveDir);
                }

                if (totalMovesMade <= 100) {
                    boolean isStuck = Arrays.stream(trackingMoves)
                            .allMatch(action -> action == Constants.Action.STAY_PUT);

                    if (isStuck && finalMove.getAction() == Constants.Action.STAY_PUT) {
                        moveDir = randomMove(neighborN, neighborE, neighborS, neighborW);
                        finalMove = Move.movement(moveDir);
                    }
                    // Record last 15 moves
                    trackingMoves[moveIndex] = finalMove.getAction();
                    moveIndex = (moveIndex + 1) % 10;
                }
                break;
        }

        // If the organism stay put while it was supposed to move, update foodMemory.
        if ((currentState == State.GOING_TO_FOOD || currentState == State.RETURNING)
                && finalMove.getAction() == Constants.Action.STAY_PUT) {
            updateFoodMemory(foodN, foodE, foodS, foodW, neighborN, neighborE, neighborS, neighborW);
            currentState = State.EXPLORING;
        }

        return finalMove;
    }

}
