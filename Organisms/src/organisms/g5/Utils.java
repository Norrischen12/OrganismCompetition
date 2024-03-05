package organisms.g5;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import organisms.Constants;
import organisms.Move;
import organisms.g3.Util;

public class Utils {

    /**
     * Populates an ArrayList with integers 1 through n, inclusive.
     * @param n The number of elements to put in the list
     * @return An {@link ArrayList} containing integers 1 through n, inclusive
     */
    public static ArrayList<Integer> randomArrayList(int n) {
        ArrayList<Integer> list = new ArrayList<>(n);
        for(int i = 1; i <= n; i++){
            list.add(i);
        }
        Collections.shuffle(list);
        return list;
    }

    public static int flipBit(int number, int bitToFlip) {
        return number ^ (1 << bitToFlip);
    }

    public static int clockwiseDirection(int inputDir) {
        if (inputDir < 1 || inputDir > 4) {
            return -1;
        } else {
            if (inputDir == 1) {
                return 3;
            } else  if (inputDir == 2) {
                return 4;
            } else  if (inputDir == 3) {
                return 2;
            } else {
                return 1;
            }
        }
    }

    public static int oppositeDirection(int inputDir) {
        if (inputDir < 1 || inputDir > 4) {
            return -1;
        } else {
            if (inputDir % 2 == 0) {
                return inputDir - 1;
            } else {
                return inputDir + 1;
            }
        }
    }

    public static int bestDirection() {
        return 0;
    }


    public static boolean isNeighbor(int neighbor) {
        return (0 <= neighbor && neighbor <= 255);
    }

    public static Constants.Action foodWithoutOpponent(boolean foodN, boolean foodE,
                                                boolean foodS, boolean foodW, int neighborN, int neighborE,
                                                int neighborS, int neighborW) {
        ArrayList<Constants.Action> locsWithFood = new ArrayList<>();
        Random random = new Random();

        if (foodN && !Util.isNeighbor(neighborN)) {
            locsWithFood.add(Constants.Action.NORTH);
        }
        if (foodW && !Util.isNeighbor(neighborW)) {
            locsWithFood.add(Constants.Action.WEST);
        }
        if (foodS && !Util.isNeighbor(neighborS)) {
            locsWithFood.add(Constants.Action.SOUTH);
        }
        if (foodE && !Util.isNeighbor(neighborE)) {
            locsWithFood.add(Constants.Action.EAST);
        }
        if (!locsWithFood.isEmpty()) {
            return locsWithFood.get(random.nextInt(locsWithFood.size()));
        }
        return null;
    }





    public static Constants.Action whichDirectionToMove(boolean foodN, boolean foodE,
                                                 boolean foodS, boolean foodW, int neighborN, int neighborE,
                                                 int neighborS, int neighborW) {

        // if there is no food on your square but food on a neighboring square, move to it (cannot be another organism there)
        Constants.Action moveToFood = Util.foodWithoutOpponent(foodN, foodE, foodS, foodW, neighborN, neighborE, neighborS, neighborW);
        if (moveToFood != null) {
            return moveToFood;
        }
        // else if there is no food anywhere move away from opponents
        // else if there is no food and no opponents move randomly
        ArrayList<Constants.Action> locsWithoutOrg = new ArrayList<>();
        Random random = new Random();

        if (!Util.isNeighbor(neighborN)) {
            locsWithoutOrg.add(Constants.Action.NORTH);
        }
        if (!Util.isNeighbor(neighborW)) {
            locsWithoutOrg.add(Constants.Action.WEST);
        }
        if (!Util.isNeighbor(neighborS)) {
            locsWithoutOrg.add(Constants.Action.SOUTH);
        }
        if (!Util.isNeighbor(neighborE)) {
            locsWithoutOrg.add(Constants.Action.EAST);
        }
        if (locsWithoutOrg.size() > 0) {
            return locsWithoutOrg.get(random.nextInt(locsWithoutOrg.size()));
        }
        return Constants.Action.STAY_PUT;
    }

    
    public static int simpleFindBestMove(boolean foodN, boolean foodE,
        boolean foodS, boolean foodW, int neighborN, int neighborE,
        int neighborS, int neighborW) {

        if (neighborW == -1 & foodW) return 1;
        if (neighborN == -1 & foodN) return 3;
        if (neighborE == -1 & foodE) return 2;
        if (neighborS == -1 & foodS) return 4;

        if (neighborW == -1) return 1;
        if (neighborN == -1) return 3;
        if (neighborE == -1) return 2;
        if (neighborS == -1) return 4;


        return 5;

    }

}
