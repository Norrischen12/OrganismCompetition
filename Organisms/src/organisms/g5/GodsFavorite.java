package organisms.g5;

import organisms.Move;
import organisms.ui.OrganismsGame;
import organisms.OrganismsPlayer;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

public class GodsFavorite implements OrganismsPlayer {
    private OrganismsGame game;
    private int dna;
    private ThreadLocalRandom random;
    //booleans to check if organisms in 4 directions exist
    private boolean hasTopLeft;
    private boolean hasTopRight;
    private boolean hasBottomLeft;
    private boolean hasBottomRight;
    //position relative to home square
    private Point position;
    private int role;
    //unimplemented
    private boolean eatStash = false;
    private int timeSinceCheck = 0;

    @Override
    public void register(OrganismsGame game, int dna) throws Exception {
        this.game = game;
        this.dna = dna == -1 ? 3000 : dna;
        this.random = ThreadLocalRandom.current();
        //interpret DNA by breaking down by digit
        int parent = getDigit(this.dna, 4);
//        hasTopRight = dna == -1 ? false : parent == 2 ? true : false;
//        hasTopLeft = dna == -1 ? false : parent == 1 ? true : false;
//        hasBottomLeft = dna == -1 ? false : parent == 0 ? true : false;
//        hasBottomRight = dna == -1 ? false : parent == 3 ? true : false;

        //set has conditions to all false if it is the initial organism.
        hasTopRight = dna == -1 ? false : true;
        hasTopLeft = dna == -1 ? false : true;
        hasBottomLeft = dna == -1 ? false : true;
        hasBottomRight = dna == -1 ? false : true;
//        role = getDigit(this.dna, 1);
        role = 3;

        position = convertDnaPoint(dna);
    }

    @Override
    public String name() {
        return "God's Favorite";
    }

    @Override
    public Color color() {
        return new Color(255, 50, 32, 255);
    }

    @Override
    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW) {

        int M = this.game.M();
        int u = this.game.u();
        int v = this.game.v();
        int K = this.game.K();
        int s = this.game.s();
        timeSinceCheck++;
//        System.out.print(role);
//        System.out.println(position.toString());

        //unimplemented
//        if (eatStash) {
//            eatStash = false;
//            updatePoint(position, 2);
//            return Move.movement(Action.EAST);
//        }

        //move back to homesquare if not on home square
        if (!position.equals(new Point(0,0))) {
            if (foodHere < 1 && energyLeft < M * .5) {
                return Move.movement(Action.STAY_PUT)
            }
            if (position.getX() >= 1 && neighborW == -1) {
                updatePoint(position, 1);
                hasTopRight = neighborN != -1;
                hasBottomRight = neighborS != -1;
                return Move.movement(Action.WEST);
            }
            if (position.getX() <= -1 && neighborE == -1) {
                hasTopLeft = neighborN != -1;
                hasBottomLeft = neighborS != -1;

                updatePoint(position, 2);
                return Move.movement(Action.EAST);
            }
            if (position.getY() >= 1 && neighborN == -1) {
                hasBottomRight = neighborE != -1;
                hasBottomLeft = neighborW != -1;
                updatePoint(position, 3);
                return Move.movement(Action.NORTH);
            }
            if (position.getY() <= -1 && neighborS == -1) {
                hasTopRight = neighborE != -1;
                hasTopLeft = neighborW != -1;
                updatePoint(position, 4);
                return Move.movement(Action.SOUTH);
            }
        }
        //go to adjacent food if energy is less than 20 moves
        if (energyLeft < M * .9) {
            int nearFood = moveToAdjFood(foodN, foodE, foodS, foodW,
                    neighborN, neighborE, neighborS, neighborW);
            //check for neighbors if you have enough energy and no adj food

            updatePoint(position, nearFood);
//            if (nearFood == 0 && energyLeft < v * 3) {
//                eatStash = true;
//                updatePoint(position, 2);
//                return Move.movement(Action.EAST);
//            }
            if (nearFood != 0) {
                return Move.movement(Action.fromInt(nearFood));
            }
        }

        if (timeSinceCheck > 10 && energyLeft > v * 20) {
            int nextMove = 0;
            if (!hasBottomLeft && !hasBottomRight) {
                nextMove = 4;
            } else if (!hasBottomLeft && !hasTopLeft) {
                nextMove = 1;
            } else if (!hasBottomRight && !hasTopRight) {
                nextMove = 2;
            } else if (!hasTopLeft && !hasTopRight) {
                nextMove = 3;
            }
            if (nextMove != 0) {
                return Move.movement(Action.fromInt(nextMove));
            }
        }
        //reproducing logic
        if (!hasTopRight && (role == 3 || role == 2)) {
            if (neighborN == -1) {
                hasTopRight = true;
                //DNA ->  role, x relative to home, y relative to home, parent
                return Move.reproduce(Action.NORTH, role == 3 ? 2200 : 3200);
            }
            if (neighborE == -1) {
                hasTopRight = true;
                return Move.reproduce(Action.EAST, role == 3 ? 2010 : 3010);
            }
        }
        if (!hasTopLeft && (role == 3 || role == 1)) {
            if (neighborN == -1) {
                hasTopLeft = true;
                return Move.reproduce(Action.NORTH, role == 3 ? 1103 : 3103);
            }
            if (neighborW == -1) {
                hasTopLeft = true;
                return Move.reproduce(Action.WEST, role == 3 ? 1013 : 3013);
            }
        }
        if (!hasBottomRight && (role == 3 || role == 1)) {
            if (neighborS == -1) {
                hasBottomRight = true;
                return Move.reproduce(Action.SOUTH, role == 3 ? 1201 : 3201);
            }
            if (neighborE == -1) {
                hasBottomRight = true;
                return Move.reproduce(Action.EAST, role == 3 ? 1021 : 3021);
            }
        }
        if (!hasBottomLeft && (role == 3 || role == 2)) {
            if (neighborS == -1) {
                hasBottomLeft = true;
                return Move.reproduce(Action.SOUTH, role == 3 ? 2102 : 3102);
            }
            if (neighborW == -1) {
                hasBottomLeft = true;
                return Move.reproduce(Action.WEST, role == 3 ? 2022 : 3022);
            }

        }
        return Move.movement(Action.STAY_PUT);

//        double reproductionThreshold = M * 0.3;

        //reproduce if there is food on the cell and current energy levels are sufficiently high
//        if (foodHere > 0) {
//            if (energyLeft > reproductionThreshold) {
//                //find best direction to reproduce in
//                int bestDir = simpleFindBestMove(foodN, foodE, foodS, foodW,
//                        neighborN, neighborE, neighborS, neighborW);
//                if (bestDir != 5) {
//                    return Move.reproduce(Action.fromInt(bestDir), this.externalState());
//                }
//                //if all adjacent cells are blocked stay put
//                return Move.movement(Action.STAY_PUT);
//            } else {
//                return Move.movement(Action.STAY_PUT);
//            }
//        } else {
//            //find the best direction to move in
//            int bestDir = simpleFindBestMove(foodN, foodE, foodS, foodW,
//                    neighborN, neighborE, neighborS, neighborW);
//            if (bestDir != 5) {
//                return Move.movement(Action.fromInt(bestDir));
//            }
//            //if all adjacent cells are blocked stay put
//            return Move.movement(Action.STAY_PUT);
//        }

    }


    @Override
    public int externalState() {
        return 99;
    }

//    public static boolean isComplete(int dna, boolean hasBottomLeft, boolean hasBottomRight,
//                                     boolean hasTopLeft, boolean hasTopRight) {
//        if (dna)
//    }


    //a simple strategy for picking moves that prioritises top/left before bottom/right
    //does not acknowledge competiton/ other organisms
    public static int moveToAdjFood(boolean foodN, boolean foodE,
                                         boolean foodS, boolean foodW, int neighborN, int neighborE,
                                         int neighborS, int neighborW) {

        if (neighborW == -1 & foodW) return 1;
        if (neighborN == -1 & foodN) return 3;
        if (neighborE == -1 & foodE) return 2;
        if (neighborS == -1 & foodS) return 4;


        return 0;

    }

    //translates position based on movement
    public static void updatePoint(Point point, int movement) {
        if (movement == 1) {
            point.translate(-1, 0);
        } else if (movement == 2) {
            point.translate(1, 0);
        } else if (movement == 3) {
            point.translate(0, -1);
        } else if (movement == 4) {
            point.translate(0, 1);
        }
        return;

    }

    //util to get a certain digit of dna
    public static int getDigit(int dna, int digit) {
        if (dna == -1) return -1;
//        System.out.println(digit);
//        System.out.println(dna);

        if (digit == String.valueOf(dna).length()) return Integer.parseInt(Integer.toString(dna).substring(digit-1));
        return Integer.parseInt(Integer.toString(dna).substring(digit-1, digit));
    }

    //converts second and third digit (x,y relative to home square) of dna to a point
    public static Point convertDnaPoint(int dna) {
        if (dna == -1) return new Point(0,0);
        int x;
        int y;
        int second = getDigit(dna, 2);
        int third = getDigit(dna, 3);
        if (second == 2) {
            x = -1;
        } else x = second;
        if (third == 2) {
            y = -1;
        } else y = third;
        return new Point(x, y);
    }

}
