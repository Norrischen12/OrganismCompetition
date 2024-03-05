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

public class FourTheWin implements OrganismsPlayer {
    private OrganismsGame game;
    private int dna;
    private ThreadLocalRandom random;
    private float p;
    private int squaresSeen = 4;
    private int foodSeen = 0;

    @Override
    public void register(OrganismsGame game, int dna) throws Exception {
        this.game = game;
        this.dna = dna;
        this.random = ThreadLocalRandom.current();
    }

    @Override
    public String name() {
        return "FourTheWin";
    }

    @Override
    public Color color() {
        return new Color(235, 170, 220, 250);
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


    private Constants.Action[] trackingMoves = new Constants.Action[15];
    private int moveIndex = 0;
    private int totalMovesMade = 0;

    @Override
    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW) {
        totalMovesMade++;
        ArrayList<Constants.Action> neighbors = new ArrayList<>();

        if (neighborS != -1) {
            neighbors.add(Action.SOUTH);
        }

        if (neighborN != -1) {
            neighbors.add(Action.NORTH);
        }

        if (neighborE != -1) {
            neighbors.add(Action.EAST);
        }

        if (neighborW != -1) {
            neighbors.add(Action.WEST);
        }
        updateP(foodN, foodE, foodS, foodW, neighborN, neighborE, neighborS, neighborW);
        boolean surroundedByFood = foodN || foodE || foodS || foodW;
        boolean hungry = foodHere <= 1 && energyLeft >= game.v();
        double reproduceThreshold = surroundedByFood ? (1-p)*((game.M() / (game.v() * 0.25))) : (1-p)*((game.M() / (game.v() * 0.16)));
        boolean reproduce = energyLeft > reproduceThreshold && energyLeft >= game.v();
        Move finalMove = Move.movement(Constants.Action.STAY_PUT); // Default move
        Constants.Action moveDir = Constants.Action.STAY_PUT;

        //Reproduction Strategy
        if (reproduce) {
            int childKey = this.random.nextInt();
            // Reproduce to the cell with food, if not available reduce to an empty cells
            moveDir = foodMove(foodN, foodE, foodS, foodW, neighborN, neighborE, neighborS, neighborW);
            if (moveDir == Constants.Action.STAY_PUT) {
                moveDir = randomMove(neighborN, neighborE, neighborS, neighborW);
            }
            if (moveDir != Action.STAY_PUT) {
                finalMove = Move.reproduce(moveDir, childKey);
                trackingMoves[moveIndex] = finalMove.getAction();
                moveIndex = (moveIndex + 1) % 15;
            }
        }

        // Food Searching Strategy
        if (hungry && finalMove.getAction() == Constants.Action.STAY_PUT) {
            moveDir = foodMove(foodN, foodE, foodS, foodW,
                    neighborN, neighborE, neighborS, neighborW);
            trackingMoves[moveIndex] = finalMove.getAction();
            moveIndex = (moveIndex + 1) % 15;
            finalMove = Move.movement(moveDir);
        }

        //Spread out for the early game, conservative for the late game
        if (totalMovesMade <= 100) {
            // Check if stuck
            boolean isStuck = Arrays.stream(trackingMoves)
                    .allMatch(action -> action == Constants.Action.STAY_PUT);

            // If stuck, choose a random move excluding neighbors
            if (isStuck && neighbors.isEmpty() && finalMove.getAction() == Constants.Action.STAY_PUT) {
                moveDir = randomMove(neighborN, neighborE, neighborS, neighborW);
                finalMove = Move.reproduce(moveDir, this.random.nextInt());
            } else if (isStuck && finalMove.getAction() == Constants.Action.STAY_PUT) {
                moveDir = randomMove(neighborN, neighborE, neighborS, neighborW);
                finalMove = Move.movement(moveDir);
            }

            // Record the move in the history
            trackingMoves[moveIndex] = finalMove.getAction();
            moveIndex = (moveIndex + 1) % 15;
        }
        return finalMove;
    }

    @Override
    public int externalState() {
        return 0;
    }
}