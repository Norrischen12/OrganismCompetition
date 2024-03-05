package organisms.ui;


class Round
{
    int number;         // what number this round is
    int foodEnergyAvailable;  // how much energy (from food) is on the board
    int totalAutomata;  // how many automata are on board
    PlayerRoundData[] players;  // what players looked like this round

    Round(int numPlayers, int roundNumber, int newFoodEnergyAvailable)
    {
        players = new PlayerRoundData[numPlayers];
        number = roundNumber;
        foodEnergyAvailable = newFoodEnergyAvailable;
        totalAutomata=0;
    }

    void addPlayerData(int player, int energy, int count)
    {
        players[player] = new PlayerRoundData(player, energy, count);
        totalAutomata+=count;
    }
}
