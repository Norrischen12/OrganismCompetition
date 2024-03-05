package organisms.g6;

import organisms.Constants;
import organisms.Move;
import organisms.OrganismsPlayer;
import organisms.ui.OrganismsGame;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static organisms.Constants.Action.SOUTH;
import static organisms.Constants.Action.STAY_PUT;

public class Zerg implements OrganismsPlayer {

    private OrganismsGame game;
    private int dna;
    private ThreadLocalRandom random;
    private int moves;
    private int reproduceConsumption;
    private int M;
    private boolean aboutToDie;

    private int[][] nestMap;

    private int numChildren;

    private MOTHER_ROLE motherRole;

    private int currXInMotherCell;

    private int currYInMotherCell;

    private int assignedXInMotherCell;

    private int assignedYInMotherCell;

    private enum SEASON {
        DEFAULT, // Default is what we had originally
        WINTER, // Winter is for more stock up & conserve energy
        SPRING // Spring is aggressive reproduction

    }

    private enum MOTHER_ROLE {
        RIGHT,
        LEFT,
        DOWN,
        UP

    }

    private final String[][] nestCellInfo = {{"00", "10", "20"},{"01", "11", "21"},{"02", "12", "22"}};

    private static Map<Integer, SEASON> dnaSeasonMap;
    //keep another map with a count of each

    @Override
    public void register(OrganismsGame game, int dna) throws Exception {
        this.game = game;
        this.dna = dna;
        this.random = ThreadLocalRandom.current();
        this.moves = 0;
        this.reproduceConsumption = game.v();
        this.M = game.M();

        registerIfMother(dna);

        dnaSeasonMap = new HashMap<>();

        //TODO: Populate mapping with other seasons... somehow?
        dnaSeasonMap.put(-3, SEASON.SPRING);
        dnaSeasonMap.put(-1, SEASON.WINTER);
        dnaSeasonMap.put(-2, SEASON.DEFAULT);

        aboutToDie = false;
    }

    @Override
    public String name() {
        return "Smart Organism";
    }

    @Override
    public Color color() {
        Color defaultColor = new Color(50, 100, 120, 255);
        return defaultColor;
    }

    // If we've identified food in one direction, say, (1, 0) [X][f][]
    // Create DNA representing the 9x9 grid around that food; bottom left of that grid is always 00
        // [ ][ ][ ]
        // [X][f][ ]   1 | 10 | 10
        // [ ][ ][ ]  num  assgn curr

        // [ ][ ][ ]
        // [ ][f][X]   1 | 21 | 21
        // [ ][ ][ ]  num  assgn curr


    @Override
    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW) {

        moves++;
        Action stay = Action.fromInt(0);

        List<Integer> directions = emptyNeighboringCells(neighborN, neighborS, neighborW, neighborE);
        if(directions.isEmpty()) {
            return Move.movement(Action.fromInt(0));
        }

        int randomAvailableDirection = directions.get(randomAvailableDirectionIndex(directions));

        //Check if there's food in 4 directions
        List<Integer> directionsWithFood = directionsWithFood(foodN, foodS, foodW, foodE);
        if(!directionsWithFood.isEmpty()) {
            int foodTarget = directionsWithFood.get(randomAvailableDirectionIndex(directionsWithFood));

            if (!isMother() && energyLeft > 500) {
                return startNest(foodTarget);
            }
        }

        if (isMother()) {
            if (!motherCellInAssignedPos()) {
                int xDiff = this.currXInMotherCell - assignedXInMotherCell;
                int yDiff = this.currYInMotherCell - assignedYInMotherCell;
                if (xDiff > 0) {
                    currXInMotherCell--;
                    return Move.movement(Action.WEST);
                } else if (xDiff < 0) {
                    currXInMotherCell++;
                    return Move.movement(Action.EAST);
                } else if (yDiff > 0) {
                    currYInMotherCell--;
                    return Move.movement(Action.NORTH);
                } else if (yDiff < 0) {
                    currYInMotherCell++;
                    return Move.movement(SOUTH);
                }
            } else if (energyLeft > 200 && numChildren < 1) {
                int whichChild = getDigit(dna, 1);
                numChildren++;
                Map.Entry<Integer, Action> childInfo = createChildDNA(whichChild + 1, this.motherRole);
                return Move.reproduce(childInfo.getValue(), childInfo.getKey()); //tweak later
            } else if(numChildren >= 1) { //We've done all the setup
                if(energyLeft < game.v() + 5) {
                    return moveMotherToCenterDir(directions);
                }
            }
        }

//        if(!isMother()) {
//            //If we have enough energy, spawn a child into a random direction
//            if(energyLeft > reproduceThreshold()) {
//                Action childPosition = Action.fromInt(randomAvailableDirection);
//                int childDna = determineChildDNA();
//                return Move.reproduce(childPosition, childDna);
//            }
//
//            //not zero so that way you can leave and come back for it to double
//            if(foodHere > 2){
//                return Move.movement(stay);
//            }
//
//            if(!directionsWithFood.isEmpty()) {
//                return Move.movement(Action.fromInt(randomAvailableDirection));
//            }
//
//            if(moveFrequency()!=0 && moves % moveFrequency() == 0 && moves < 100) { //If we haven't moved in the past moveFreq moves, move
//                return Move.movement(Action.fromInt(randomAvailableDirection));
//            }
//        }


        return Move.movement(Action.fromInt(0));

    }

    private Move moveMotherToCenterDir(List<Integer> availableDirections) {
        Action centerDir = Action.EAST;
        int proposedY = currYInMotherCell;
        int proposedX = currXInMotherCell;

        if(motherRole == MOTHER_ROLE.UP) {
            proposedY++;
            centerDir = Action.SOUTH;
        } else if(motherRole == MOTHER_ROLE.DOWN) {
            proposedY--;
            centerDir = Action.NORTH;
        } else if(motherRole == MOTHER_ROLE.RIGHT) {
            proposedX--;
            centerDir = Action.WEST;
        }else if(motherRole == MOTHER_ROLE.LEFT) {
            proposedX++;
            centerDir = Action.EAST;
        }

        if(!availableDirections.contains(centerDir.intValue())) {
            return Move.movement(STAY_PUT);
        } else {
            currYInMotherCell = proposedY;
            currXInMotherCell = proposedX;
            return Move.movement(centerDir);
        }
    }

    private boolean isMother() {
        return this.motherRole != null;
    }

    private void updateIfAboutToDie(List<Integer> directionsWithFood, List<Integer> directionsToMove, int energyLeft) {
        //Can we move to a tile with food?
        //TODO: Diana implement logic here
    }

    private boolean motherCellInAssignedPos(){
        return (assignedXInMotherCell == currXInMotherCell) && (assignedYInMotherCell == currYInMotherCell);
    }

    Map.Entry<Integer, Action> createChildDNA(int numChild, MOTHER_ROLE currMotherRole) {
        StringBuilder b = new StringBuilder();
        b.append(numChild);
        Action spawnDirection = SOUTH;
        if(currMotherRole == MOTHER_ROLE.RIGHT) {
            b.append(1222);
            spawnDirection = SOUTH;
        } else if(currMotherRole == MOTHER_ROLE.LEFT) {
            b.append(1000);
            spawnDirection = Action.NORTH;
        } else if(currMotherRole == MOTHER_ROLE.UP) {
            b.append(2120);
            spawnDirection = Action.EAST;
        } else if(currMotherRole == MOTHER_ROLE.DOWN) {
            b.append("0102");
            spawnDirection = Action.WEST;
        }
        Map.Entry<Integer, Action> directions = new AbstractMap.SimpleEntry<Integer, Action>(
                Integer.parseInt(b.toString()),
                spawnDirection);

        return directions;

    }
    private Move startNest(int foodTarget){
        int childDna; //We're a left parent, start an up child
        Action childDir;
        //Mark our location, determine, motherRole update our DNA
        if(foodTarget == Action.WEST.intValue()) {
            this.motherRole = MOTHER_ROLE.RIGHT;
            dna = 12121;
            this.assignedXInMotherCell = 2;
            this.assignedYInMotherCell = 1;
            this.currXInMotherCell = 2;
            this.currYInMotherCell = 1;
        } else if(foodTarget == Action.EAST.intValue()) {
            this.motherRole = MOTHER_ROLE.LEFT;
            dna = 10101; ///
            this.assignedXInMotherCell = 0;
            this.assignedYInMotherCell = 1;
            this.currXInMotherCell = 0;
            this.currYInMotherCell = 1;
        } if(foodTarget == SOUTH.intValue()) {
            this.motherRole = MOTHER_ROLE.UP;
            dna = 11010;
            this.assignedXInMotherCell = 1;
            this.assignedYInMotherCell = 0;
            this.currXInMotherCell = 1;
            this.currYInMotherCell = 0;
        } if(foodTarget == Action.NORTH.intValue()) {
            this.motherRole = MOTHER_ROLE.DOWN;
            dna = 11212;
            this.assignedXInMotherCell = 1;
            this.assignedYInMotherCell = 2;
            this.currXInMotherCell = 1;
            this.currYInMotherCell = 2;
        }

        Map.Entry<Integer, Action> childInfo = createChildDNA(2, this.motherRole);
        numChildren++;
        return Move.reproduce(childInfo.getValue(), childInfo.getKey()); //tweak later
    }

    // UTILITY FUNCTIONS ----------------------------------------------------------------------------------------

    public static int getDigit(int dna, int digit) {
        if (dna == -1) return -1;

        if (digit == String.valueOf(dna).length()) return Integer.parseInt(Integer.toString(dna).substring(digit-1));
        return Integer.parseInt(Integer.toString(dna).substring(digit-1, digit));
    }


    //TODO: Return Spring, Winter, and Default DNA keys depending on some determinant
    private int determineChildDNA() {
        int num = random.nextInt(4);
        num *= -1;
        ArrayList<Integer> numbersList = new ArrayList<Integer>();
        numbersList.add(-1);
        numbersList.add(-2);
        numbersList.add(-3);
        if(num==0){
            return numbersList.get(random.nextInt(numbersList.size()));
        }else{
            return this.dna;
        }
    }

    private List<Integer> directionsWithFood(boolean foodN, boolean foodS, boolean foodW, boolean foodE) {
        List<Integer> foodNeighbors = new ArrayList<>();
        if(foodN) {
            foodNeighbors.add(3);
        }
        if(foodS) {
            foodNeighbors.add(4);
        }
        if(foodW) {
            foodNeighbors.add(1);
        }
        if(foodE) {
            foodNeighbors.add(2);
        }
        return foodNeighbors;
    }

    private List<Integer> emptyNeighboringCells(int neighborN, int neighborS, int neighborW, int neighborE) {
        List<Integer> neighbors = new ArrayList<>();
        //If there's no neighbors, we can consider moving to that spot
        if(neighborN == -1) {
            neighbors.add(3);
        }
        if(neighborS == -1) {
            neighbors.add(4);
        }
        if(neighborW == -1) {
            neighbors.add(1);
        }
        if(neighborE == -1) {
            neighbors.add(2);
        }
        return neighbors;
    }


    private int randomAvailableDirectionIndex(List<Integer> directions) {
        return this.random.nextInt(directions.size());
    }

    private int moveFrequency() {
        if(dnaSeasonMap.get(dna) == SEASON.WINTER) {
            return 0;
        } else if(dnaSeasonMap.get(dna) == SEASON.SPRING) { //Move a lot more in spring
            return 5;
        }

        return 10; //Default
    }

    private int reproduceThreshold() {
        if(dnaSeasonMap.get(dna) == SEASON.WINTER) {
            return (int) ((M / 3.0) + this.reproduceConsumption);
        } else if(dnaSeasonMap.get(dna) == SEASON.SPRING) {
            return 150;
        }

        return 300;
    }

    @Override
    public int externalState() {
        return 0; //TODO: Consider changing ?
    }

    private void registerIfMother(int dna) {
        if(dna >= 100) { //for now, only mothers have dna > 100
            int numMother = getDigit(dna, 1);

            this.currXInMotherCell =  getDigit(dna, 4);
            this.currYInMotherCell = getDigit(dna, 5);

            this.assignedXInMotherCell =  getDigit(dna, 2);
            this.assignedYInMotherCell = getDigit(dna, 3);

            if(assignedXInMotherCell == 2 && assignedYInMotherCell == 1) {
                this.motherRole = MOTHER_ROLE.RIGHT;
            } else if(assignedXInMotherCell == 0 && assignedYInMotherCell == 1) {
                this.motherRole = MOTHER_ROLE.LEFT;
            } else if(assignedXInMotherCell == 1 && assignedYInMotherCell == 2) {
                this.motherRole = MOTHER_ROLE.DOWN;
            } else if(assignedXInMotherCell == 1 && assignedYInMotherCell == 0)
                this.motherRole = MOTHER_ROLE.UP;
            }
    }
}
