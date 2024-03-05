
package organisms.g6;

import organisms.Move;
import organisms.OrganismsPlayer;
import organisms.g3.Util;
import organisms.ui.OrganismsGame;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static organisms.g6.SmartOrganism.SEASON.*;

public class SmartOrganism implements OrganismsPlayer {

    private OrganismsGame game;
    private int dna;
    private ThreadLocalRandom random;
    private int moves;
    private int reproduceConsumption;
    private int M;
    private boolean aboutToDie;
    private LinkedList<String> visitedPositions = new LinkedList<>();
    private int currx = 0;
    private int curry = 0;

    enum SEASON {
        DEFAULT, // Default is what we had originally
        WINTER, // Winter is for more stock up & conserve energy
        SPRING // Spring is aggressive reproduction

    }

    private static HashMap<Integer, SEASON> dnaSeasonMap;
    private static HashMap<SEASON, Integer> seasonDnaMap;
    //keep another map with a count of each

    private static LinkedList<SEASON> mostRecentlySeenDeaths;
    private static HashMap<SEASON, Double> dnaFrequencyMap;
    private static HashMap<SEASON, Double> dnaDeathRateMap;
    static int memoryCapacity = 10;

    @Override
    public void register(OrganismsGame game, int dna) throws Exception {
        this.game = game;
        this.dna = dna;
        this.random = ThreadLocalRandom.current();
        this.moves = 0;
        this.reproduceConsumption = game.v();
        this.M = game.M();

        dnaSeasonMap = new HashMap<>();

        seasonDnaMap = new HashMap<>();

        dnaSeasonMap.put(-3, SPRING);
        seasonDnaMap.put(SPRING, 3);

        dnaSeasonMap.put(-1, WINTER);
        seasonDnaMap.put(WINTER, -1);

        dnaSeasonMap.put(-2, DEFAULT);
        seasonDnaMap.put(DEFAULT, -2);


        mostRecentlySeenDeaths = new LinkedList<>();

        dnaFrequencyMap = new HashMap<>();
        dnaFrequencyMap.put(WINTER, 0.0);
        dnaFrequencyMap.put(DEFAULT, 0.0);
        dnaFrequencyMap.put(SPRING, 0.0);

        dnaFrequencyMap.put(dnaSeasonMap.get(dna), 1.0);

        dnaDeathRateMap = new HashMap<>();

        aboutToDie = false;
    }

    @Override
    public String name() {
        return "Smart Organism";
    }

    @Override
    public Color color() {
        Color defaultColor = new Color(50, 100, 120, 255);
        if(dnaSeasonMap.get(dna) == WINTER) {
            defaultColor = defaultColor.darker(); //Winter organisms are darker
        } else if(dnaSeasonMap.get(dna) == SEASON.SPRING){
            defaultColor = defaultColor.brighter(); //Spring organisms are brighter
        }
        return defaultColor;
    }

    private boolean wasVisitedBefore(int x, int y) {
        return visitedPositions.contains((currx + x) + "," + (curry + y));
    }

    @Override
    public Move move(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW) {

        moves++;
        Action stay = Action.fromInt(0);

        List<Integer> directions = neighboringCells(neighborN, neighborS, neighborW, neighborE);
        if(directions.isEmpty()) {
            //If we're trapped, we can't move
            return Move.movement(Action.fromInt(0));
        }

        int randomAvailableDirection = directions.get(randomAvailableDirectionIndex(directions));

        if(wasVisitedBefore(0, -1) && directions.size() > 1) directions.remove((Integer) 3);
        if(wasVisitedBefore(0, 1) && directions.size() > 1) directions.remove((Integer) 4);
        if(wasVisitedBefore(-1, 0) && directions.size() > 1) directions.remove((Integer) 1);
        if(wasVisitedBefore(1, 0) && directions.size() > 1) directions.remove((Integer) 2);

        //Check if there's food in 4 directions
        List<Integer> directionsWithFood = directionsWithFood(foodN, foodS, foodW, foodE);
        if(!directionsWithFood.isEmpty()) {
            randomAvailableDirection = directionsWithFood.get(randomAvailableDirectionIndex(directionsWithFood));
        }


        switch(randomAvailableDirection) {
            case 1:
                currx -= 1;
                break;
            case 2:
                currx += 1;
                break;
            case 3:
                curry -= 1;
                break;
            case 4:
                curry += 1;
                break;
        }

        visitedPositions.addLast(currx + "," + curry);
        if(visitedPositions.size() > memoryCapacity) {
            visitedPositions.removeFirst();
        }

//        if(dnaSeasonMap.get(dna) == SEASON.WINTER) {
//            return this.desertMove( foodHere,  energyLeft,  foodN,  foodE,
//                    foodS,  foodW,  neighborN,  neighborE,
//                    neighborS,  neighborW);
//        }



        //If we have enough energy, spawn a child into a random direction
        if(energyLeft > reproduceThreshold()) {
            Action childPosition = Action.fromInt(randomAvailableDirection);
            int childDna = determineChildDNA();

            return Move.reproduce(childPosition, childDna);
        }

        //not zero so that way you can leave and come back for it to double
        if(foodHere > 2){
            return Move.movement(stay);
        }

        if(!directionsWithFood.isEmpty()) {
            return Move.movement(Action.fromInt(randomAvailableDirection));
        }

        if(moveFrequency ()!= 0 && moves % moveFrequency() == 0 && moves < 100) { //If we haven't moved in the past moveFreq moves, move
            return Move.movement(Action.fromInt(randomAvailableDirection));
        }

        updateIfAboutToDie(directionsWithFood, energyLeft);

        return Move.movement(Action.fromInt(0));

    }

    // UTILITY FUNCTIONS ----------------------------------------------------------------------------------------

    private void updateIfAboutToDie(List<Integer> directionsWithFood, int energyLeft) {
        if(energyLeft < game.v()) { //If we don't even have energy to move
            aboutToDie = true;
        }

        if(energyLeft > game.v() && !directionsWithFood.isEmpty()) {
            aboutToDie = false;
        }
    }



    private int numFood(boolean foodN, boolean foodE, boolean foodS, boolean foodW) {
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

    private Move desertMove(int foodHere, int energyLeft, boolean foodN, boolean foodE,
                            boolean foodS, boolean foodW, int neighborN, int neighborE,
                            int neighborS, int neighborW){
        if (foodHere >= 1) {
            return Move.movement(Action.WEST);
        }

        if (foodE && !Util.isNeighbor(neighborE) && energyLeft > this.game.M() - this.game.u()) {
            return Move.reproduce(Action.WEST, dna);
        }
        if (numFood(foodN, foodE, foodS, foodW) > 2 && energyLeft > this.game.v()) {
            int randomChoice = random.nextInt(2);
            if (randomChoice == 0) {
                int childDna = determineChildDNA();

                return Move.reproduce(Action.NORTH, childDna);
            } else {
                int childDna = determineChildDNA();
                return Move.reproduce(Action.WEST, childDna);
            }
        }

        List<Integer> directionsWithFood = directionsWithFood(foodN, foodS, foodW, foodE);

        int randomAvailableDirection = 0;
        if(!directionsWithFood.isEmpty()) {
            randomAvailableDirection = directionsWithFood.get(randomAvailableDirectionIndex(directionsWithFood));
        }
        Move moveOntoFood = Move.movement(Action.fromInt(randomAvailableDirection));
        if (moveOntoFood != null) {
            if (energyLeft <= 21) {
                return moveOntoFood;
            } else {
                return Move.movement(Action.STAY_PUT);
            }
        }

        // pick randomly between north and west and staying put,
        int randomChoice = random.nextInt(14);
        if (randomChoice == 0) {
            return Move.movement(Action.NORTH);
        } else if (randomChoice == 1) {
            return Move.movement(Action.WEST);
        } else {

            return Move.movement(Action.STAY_PUT);
        }

    }
    //TODO: Return Spring, Winter, and Default DNA keys depending on some determinant
    private int determineChildDNA() {
        int num = random.nextInt(8);
        num *= -1;
        ArrayList<Integer> numbersList = new ArrayList<Integer>();
        numbersList.add(-1);
        numbersList.add(-1);
        numbersList.add(-2);
        numbersList.add(-3);

        SEASON unfavoredSeason = Collections.max(recentDeathRatePerSeason().entrySet(), Map.Entry.comparingByValue()).getKey();
        //numbersList.removeIf(i -> i.equals(seasonDnaMap.get(unfavoredSeason)));

        if(num == 0){
            return numbersList.get(random.nextInt(numbersList.size()));
        } else{
            return this.dna;
        }
    }

    HashMap<SEASON, Double> recentDeathRatePerSeason() {
        double winterDeaths = 0;
        double defaultDeaths = 0;
        double springDeaths = 0;

        for (SEASON s: mostRecentlySeenDeaths) {
            if(s == WINTER) {
                winterDeaths++;
            }
            if(s == DEFAULT) {
                defaultDeaths++;
            }
            if(s == SPRING) {
                springDeaths++;
            }
        }

        double totalDeaths = winterDeaths + defaultDeaths + springDeaths;

        HashMap<SEASON, Double> deathRatePerSeason = new HashMap();
        deathRatePerSeason.put(WINTER, winterDeaths/totalDeaths);
        deathRatePerSeason.put(SPRING, springDeaths/totalDeaths);
        deathRatePerSeason.put(DEFAULT, defaultDeaths/totalDeaths);
        return deathRatePerSeason;
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

    private List<Integer> neighboringCells(int neighborN, int neighborS, int neighborW, int neighborE) {
        List<Integer> neighbors = new ArrayList<>();
        //If there's no neighbors, we can consider moving to that spot
        if(neighborN == -1) {
            neighbors.add(3);
            interpretExternalState(neighborN);
        }
        if(neighborS == -1) {
            neighbors.add(4);
            interpretExternalState(neighborS);
        }
        if(neighborW == -1) {
            neighbors.add(1);
            interpretExternalState(neighborW);
        }
        if(neighborE == -1) {
            neighbors.add(2);
            interpretExternalState(neighborE);
        }

        List<Integer> neighbors2 = new ArrayList<>(neighbors);
        neighbors2.removeIf(i -> (getDigit(i, 1) == 4)); //remove directions with dying similar neighbors

        if(!neighbors2.isEmpty()) {
            return neighbors2;
        }

        return neighbors;
    }


    private int randomAvailableDirectionIndex(List<Integer> directions) {
        return this.random.nextInt(directions.size());
    }

    private int moveFrequency() {
        if(dnaSeasonMap.get(dna) == WINTER) {
            return 0;
        } else if(dnaSeasonMap.get(dna) == SEASON.SPRING) { //Move a lot more in spring
            return 5;
        }

        return 10; //Default
    }

    private int reproduceThreshold() {
        if(dnaSeasonMap.get(dna) == WINTER) {
            return (int) ((M / 3.0) + this.reproduceConsumption);
        } else if(dnaSeasonMap.get(dna) == SEASON.SPRING) {
            return 150;
        }

        return 300;
    }

    @Override
    public int externalState() {
        StringBuilder b = new StringBuilder();

        if(aboutToDie) {
            b.append("4");
        } else {
            b.append("8");
        }

        if(dnaSeasonMap.get(dna).equals(WINTER)) {
            b.append("1");
        } else if(dnaSeasonMap.get(dna).equals(SEASON.DEFAULT)) {
            b.append("2");
        } else {
            b.append("3");
        }

        return Integer.parseInt(b.toString());

    }

    private void interpretExternalState(int externalState) {
        SEASON neighborSeason = SPRING;
        if(getDigit(externalState, 2) == 1) { //If the organism is winter
            neighborSeason = WINTER;

        } else if(getDigit(externalState, 2) == 2) { //If the organism is default
            neighborSeason = DEFAULT;
        } else if (getDigit(externalState, 2) == 3) { //If the organism is Spring
            neighborSeason = SPRING;
        }

        if(getDigit(externalState, 1) == 4) { //If the organism is about to die
            mostRecentlySeenDeaths.add(neighborSeason);
            if(mostRecentlySeenDeaths.size() > memoryCapacity) {
                mostRecentlySeenDeaths.removeFirst();
            }
        }

        //dnaFrequencyMap.put(neighborSeason, dnaFrequencyMap.get(neighborSeason) + 1);
    }

    public static int getDigit(int dna, int digit) {
        if (dna == -1) return -1;

        if (digit == String.valueOf(dna).length()) return Integer.parseInt(Integer.toString(dna).substring(digit-1));
        return Integer.parseInt(Integer.toString(dna).substring(digit-1, digit));
    }


}