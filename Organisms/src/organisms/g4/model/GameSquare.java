package organisms.g4.model;

public class GameSquare {

    private int numberOfTimeVisited = 0;
    private int numberOfTimeFood = 0;
    private int numberOfTimePlayer = 0;

    public double getLocalProbabilityOfFood() {
        double maxProbabilityOfFood = 0.1;
        double minProbabilityOfFood = 0.001;
        double calculatedP = (numberOfTimeVisited != 0) ? (double) numberOfTimeFood / numberOfTimeVisited : 0;
        if (calculatedP == 0) return 0;
        double p = Math.min(calculatedP, maxProbabilityOfFood);
        return Math.max(p, minProbabilityOfFood);
    }

    public void incrementNumberOfTimeVisited() {
        numberOfTimeVisited++;
    }

    public void incrementNumberOfTimeFood() {
        numberOfTimeFood++;
    }

    public void incrementNumberOfTimePlayer() {
        numberOfTimePlayer++;
    }

    public int getNumberOfTimeVisited() {
        return numberOfTimeVisited;
    }

    public int getNumberOfTimeFood() {
        return numberOfTimeFood;
    }

}
