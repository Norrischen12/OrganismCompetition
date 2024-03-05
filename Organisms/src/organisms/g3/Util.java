package organisms.g3;

import organisms.Constants;

import java.util.ArrayList;
import java.util.*;

public class Util {
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

    public static Constants.Action directToReproduce(int neighborN, int neighborE, int neighborS, int neighborW) {
        ArrayList<Constants.Action> locsWithoutOp = new ArrayList<>();
        Random random = new Random();

        if (!Util.isNeighbor(neighborN)) {
            locsWithoutOp.add(Constants.Action.NORTH);
        }
        if (!Util.isNeighbor(neighborW)) {
            locsWithoutOp.add(Constants.Action.WEST);
        }
        if (!Util.isNeighbor(neighborS)) {
            locsWithoutOp.add(Constants.Action.SOUTH);
        }
        if (!Util.isNeighbor(neighborE)) {
            locsWithoutOp.add(Constants.Action.EAST);
        }
        if (!locsWithoutOp.isEmpty()) {
            return locsWithoutOp.get(random.nextInt(locsWithoutOp.size()));
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

    public static int getLastDigit(int number) {
        return Math.abs(number % 10);
    }

    public static int getFirstDigit(int number) {
        number = Math.abs(number);
        if (number < 10) {
            return 0;
        } else {
            return (int) Math.floor(((double) number) / 10);
        }
    }

}
