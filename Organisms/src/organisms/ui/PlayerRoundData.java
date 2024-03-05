package organisms.ui;

class PlayerRoundData {

    int playerId;
    int energy;
    int count;
    boolean extinction;
    int extinctionCount;

    PlayerRoundData(int p, int e, int c) {
        playerId = p;
        energy = e;
        count = c;
        extinction = false;
        extinctionCount = 0;
    }

    public void isExtinct(boolean extinction) {
        this.extinction = extinction;
    }

}