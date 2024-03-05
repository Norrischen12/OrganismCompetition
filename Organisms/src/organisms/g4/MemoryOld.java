package organisms.g4;

import java.util.Arrays;
import organisms.Constants;
import organisms.Move;
import organisms.OrganismsPlayer;
import organisms.ui.OrganismsGame;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

public class MemoryOld implements OrganismsPlayer {
    private OrganismsGame game;
    private int dna;
    private ThreadLocalRandom random;
    private float p;
    private int squaresSeen = 4;
    private int foodSeen = 0;

    private int policy;
    @Override
    public void register(OrganismsGame game, int dna) throws Exception {
        this.game = game;
        this.dna = dna;
        this.random = ThreadLocalRandom.current();
    }

    @Override
    public String name() {
        return "MemoryOld";
    }

    @Override
    public Color color() {
        return new Color(150, 100, 200, 250);
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

    @Override
    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW) {
        totalMovesMade++;
        updateP(foodN, foodE, foodS, foodW, neighborN, neighborE, neighborS, neighborW);

        Move finalMove = Move.movement(Constants.Action.STAY_PUT); // Default move.
        Constants.Action moveDir = Constants.Action.STAY_PUT;
        ArrayList<Constants.Action> neighs = neighbors(neighborN, neighborE, neighborS, neighborW);

        boolean hungry = foodHere < 1 && energyLeft >= game.v();
        boolean surroundedByFood = foodN || foodE || foodS || foodW;
        double reproduceThreshold = surroundedByFood ? (1-p)*((game.M() / (game.v() * 0.4))) : (1-p)*(game.M() / (game.v() * 0.3));
        boolean reproduce = energyLeft > reproduceThreshold && energyLeft >= game.v();
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

    @Override
    public int externalState () {
        return 0;
    }
}