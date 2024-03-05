package organisms.g1;

import organisms.Move;
import organisms.OrganismsPlayer;
import organisms.ui.OrganismsGame;

import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class BH implements OrganismsPlayer {
    private OrganismsGame game;
    private int dna;
    private ThreadLocalRandom random;
    private int moves;

    private Set<String> visitedPositions = new HashSet<>();
    private int currx = 0;
    private int curry = 0;

    @Override
    public void register(OrganismsGame game, int dna) throws Exception {
        this.game = game;
        this.dna = dna;
        this.random = ThreadLocalRandom.current();
        this.moves = 0;
    }

    @Override
    public String name() {
        return "BH";
    }

    @Override
    public Color color() {
        return new Color(20, 134, 12, 155);
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
        if(foodHere>2){
            return Move.movement(stay);
        }

        List<Integer> directions = new ArrayList<>();
        if(neighborN == -1) {
            directions.add(3);
        }
        if(neighborS == -1) {
            directions.add(4);
        }
        if(neighborW == -1) {
            directions.add(1);
        }
        if(neighborE == -1) {
            directions.add(2);
        }

        if(directions.isEmpty()) {
            return Move.movement(Action.fromInt(0));
        }

        int randomAvailableDirectionIndex = this.random.nextInt(directions.size());
        int randomAvailableDirection = directions.get(randomAvailableDirectionIndex);

        if(wasVisitedBefore(0, -1)) directions.remove((Integer)3);
        if(wasVisitedBefore(0, 1)) directions.remove((Integer)4);
        if(wasVisitedBefore(-1, 0)) directions.remove((Integer)1);
        if(wasVisitedBefore(1, 0)) directions.remove((Integer)2);

        if(directions.size() > 0 && this.random.nextFloat() < 0.8) {
            randomAvailableDirectionIndex = this.random.nextInt(directions.size());
            randomAvailableDirection = directions.get(randomAvailableDirectionIndex);
        }

        // Update the current position before making a move
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

        visitedPositions.add(currx + "," + curry);

        if(energyLeft > 300) {
            Action childPosition = Action.fromInt(randomAvailableDirection);
            int childKey = this.random.nextInt();
            return Move.reproduce(childPosition, childKey);
        }

        List<Integer> directionsWithFood = new ArrayList<>();
        if(foodN) directionsWithFood.add(3);
        if(foodS) directionsWithFood.add(4);
        if(foodW) directionsWithFood.add(1);
        if(foodE) directionsWithFood.add(2);

        if(!directionsWithFood.isEmpty()) {
            randomAvailableDirectionIndex = this.random.nextInt(directionsWithFood.size());
            randomAvailableDirection = directionsWithFood.get(randomAvailableDirectionIndex);
            return Move.movement(Action.fromInt(randomAvailableDirection));
        }

        if(moves % 10 == 0 && moves < 100) {
            return Move.movement(Action.fromInt(randomAvailableDirection));
        }

        return Move.movement(Action.fromInt(0));
    }

    @Override
    public int externalState() {
        return 0;
    }
}
