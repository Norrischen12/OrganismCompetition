package organisms.g5;

import organisms.Move;
import organisms.ui.OrganismsGame;
import organisms.OrganismsPlayer;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

public class Venus implements OrganismsPlayer {
    private OrganismsGame game;
    private int dna;
    private ThreadLocalRandom random;
    private boolean has1;
    private boolean has2;
    private boolean has3;
    private boolean has4;
    private int counter1;
    private int counter2;
    private Point position;
    private int role;
    private boolean eatStash = false;
    private boolean trap = false;

    @Override
    public void register(OrganismsGame game, int dna) throws Exception {
        this.game = game;
        this.dna = dna == -1 ? 1000 : dna;
        this.random = ThreadLocalRandom.current();
        int parent = getDigit(this.dna, 4);
        role = getDigit(this.dna, 1);
//        has1 = dna == -1 ? false : parent == 1 ? true : false;
//        has2 = dna == -1 ? false : parent == 2 ? true : false;
//        has3 = dna == -1 ? false : parent == 3 ? true : false;
//        has4 = dna == -1 ? false : parent == 4 ? true : false;
        has1 = dna == -1 ? false : true;
        has2 = dna == -1 ? false : true;
        has3 = dna == -1 ? false : true;
        has4 = dna == -1 ? false : true;

//        role = 3;

        position = convertDnaPoint(dna);
    }

    @Override
    public String name() {
        return "Venus Flytrap";
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
//        System.out.print(role);
//        System.out.println(position.toString());
//        System.out.print(role);
//        System.out.println(trap);
        if (trap) {
            if (role == 2) {
                if (neighborN == -1) {
                    trap = false;
                    Move move = moveTowards(position, new Point(0, 0), neighborN, neighborE, neighborS, neighborW);
                    updatePoint(position, move);
                    return move;
                }
                Move move = moveTowards(position, new Point(1, 0), neighborN, neighborE, neighborS, neighborW);
                updatePoint(position, move);
                return move;
            }
            if (role == 3) {
                if (neighborS == -1 && position.equals(new Point(-1, 0))) {
                    trap = false;
                    Move move = moveTowards(position, new Point(0, 0), neighborN, neighborE, neighborS, neighborW);
                    updatePoint(position, move);
                    return move;
                }
                Move move = moveTowards(position, new Point(-1, 0), neighborN, neighborE, neighborS, neighborW);
                updatePoint(position, move);
                return move;
            }
            if (role == 1) {
                if (neighborE == -1 && position.equals(new Point(0, 0))) {
                    trap = false;
                }
                Move move = moveTowards(position, new Point(0, 0), neighborN, neighborE, neighborS, neighborW);
                updatePoint(position, move);
                return move;
            }
            if (role == 4) {
                if (neighborW == -1  && position.equals(new Point(-1, 0))) {
                    trap = false;
                }
                Move move = moveTowards(position, new Point(0, 0), neighborN, neighborE, neighborS, neighborW);
                updatePoint(position, move);
                return move;
            }

        }

        if (position.equals(new Point(0,0))) {
            if (role == 2) {
                if (neighborN == -1) {
                    counter1++;
                    if (counter1 >= 2) {
                        has1 = false;
                        counter1 = 0;
                    }
                } else {
                    has1 = true;
                    counter1 = 0;
                }
                if (neighborN == 222){
                    trap = true;
                }
            }
            if (role == 1) {
                if (neighborS == -1) {
                    counter1++;
                    if (counter1 >= 2) {
                        has2 = false;
                        counter1 = 0;
                    }
                } else {
                    has2 = true;
                    counter1 = 0;
                }
                if (neighborE != 4 && neighborE != -1) {
                    trap = true;
                }
            }
            if (role == 4) {
                if (neighborN == -1) {
                    counter1++;
                    if (counter1 >= 2) {
                        has3 = false;
                        counter1 = 0;
                    }
                } else {
                    has3 = true;
                    counter1 = 0;
                }
                if (neighborW != 1 && neighborE != -1) {
                    trap = true;
                }
            }
            if (role == 3) {
                if (neighborS == -1) {
                    counter1++;
                    if (counter1 >= 2) {
                        has4 = false;
                        counter1 = 0;
                    }
                } else {
                    has4 = true;
                    counter1 = 0;
                }
                if (neighborS == 222){
                    trap = true;
                }
            }
        }
        if (position.equals(new Point(1,0)) && role == 1) {
            if (neighborE == -1) {
                counter2++;
                if (counter2 >= 2) {
                    has4 = false;
                    counter2 = 0;
                }
            } else {
                has4 = true;
                counter2 = 0;
            }
        }
        if (position.equals(new Point(-1,0)) && role == 4) {
            if (neighborW == -1) {
                counter2++;
                if (counter2 >= 2) {
                    has1 = false;
                    counter2 = 0;
                }
            } else {
                has1 = true;
                counter2 = 0;
            }
        }




        if (!position.equals(new Point(0,0)) && !trap) {

            if (position.getX() >= 1 && neighborW == -1) {
                updatePoint(position, 1);
                return Move.movement(Action.WEST);
            }
            if (position.getX() <= -1 && neighborE == -1) {
                updatePoint(position, 2);
                return Move.movement(Action.EAST);
            }
            if (position.getY() >= 1 && neighborN == -1) {
                updatePoint(position, 3);
                return Move.movement(Action.NORTH);
            }
            if (position.getY() <= -1 && neighborS == -1) {
                updatePoint(position, 4);
                return Move.movement(Action.SOUTH);
            }
            return Move.movement(Action.STAY_PUT);
        }

        if (energyLeft < v * 20) {
            int nearFood = moveToAdjFood(foodN, foodE, foodS, foodW,
                    neighborN, neighborE, neighborS, neighborW);
            updatePoint(position, nearFood);
//            if (nearFood == 0 && energyLeft < v * 3) {
//                eatStash = true;
//                updatePoint(position, 2);
//                return Move.movement(Action.EAST);
//            }
            return Move.movement(Action.fromInt(nearFood));
        }

        if (!has1) {
            if (role == 2 && neighborN == -1) {
                has1 = true;
                return Move.reproduce(Action.NORTH, 1002);
            }
            if (role == 4 && neighborW == -1) {
                has1 = true;
                return Move.reproduce(Action.EAST, 1104);
            }
        }
        if (!has2) {
            if (role == 1 && neighborS == -1) {
                has2 = true;
                return Move.reproduce(Action.SOUTH, 2001);
            }
        }
        if (!has4) {
            if (role == 1 && neighborE == -1) {
                has4 = true;
                return Move.reproduce(Action.EAST, 4201);
            }
            if (role == 3 && neighborS == -1) {
                has4 = true;
                return Move.reproduce(Action.SOUTH, 4003);
            }
        }
        if (!has3) {
            if (role == 4 && neighborN == -1) {
                has3 = true;
                return Move.reproduce(Action.NORTH, 3004);
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
        return trap ? 222 : role;
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
    public static void updatePoint(Point point, Move movement) {
        if (movement == Move.movement(Action.WEST)) {
            point.translate(-1, 0);
        } else if (movement == Move.movement(Action.EAST)) {
            point.translate(1, 0);
        } else if (movement == Move.movement(Action.NORTH)) {
            point.translate(0, -1);
        } else if (movement == Move.movement(Action.SOUTH)) {
            point.translate(0, 1);
        }
        return;

    }
    public static int getDigit(int dna, int digit) {
        if (dna == -1) return -1;
//        System.out.println(digit);
//        System.out.println(dna);

        if (digit == String.valueOf(dna).length()) return Integer.parseInt(Integer.toString(dna).substring(digit-1));
        return Integer.parseInt(Integer.toString(dna).substring(digit-1, digit));
    }

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

    public static Move moveTowards(Point curr, Point destination, int neighborN, int neighborE, int neighborS, int neighborW) {
        if (!curr.equals(destination)) {

            if (curr.getX() - destination.getX() >= 1 && neighborW == -1) {
                updatePoint(curr, 1);
                return Move.movement(Action.WEST);
            }
            if (curr.getX() - destination.getX() <= -1 && neighborE == -1) {
                updatePoint(curr, 2);
                return Move.movement(Action.EAST);
            }
            if (curr.getY() - destination.getY() >= 1 && neighborN == -1) {
                updatePoint(curr, 3);
                return Move.movement(Action.NORTH);
            }
            if (curr.getY() - destination.getY() <= -1 && neighborS == -1) {
                updatePoint(curr, 4);
                return Move.movement(Action.SOUTH);
            }

        }
        return Move.movement(Action.STAY_PUT);
    }


}
