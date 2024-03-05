package organisms.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Vector;

class GraphDrawer extends JFrame {
    int width = 800;
    int height = 600;
    Vector rounds;
    Color[] playerColors;

    public GraphDrawer(Vector newRounds, Color newPlayerColors[]) {
        super("Power Graphs: Automata, Energy, Available Food");
        rounds = newRounds;
        playerColors = newPlayerColors;
        getContentPane().setBackground(Color.BLACK);
        setSize(width, height);
        setVisible(true);
    }

    public void paint(Graphics g) {
        super.paint(g);

        // if can't paint, don't try
        if (rounds == null || rounds.size() == 0) {
            return;
        }

        int numPlayers = ((Round) rounds.elementAt(0)).players.length;

        int xFoodEnergy[] = new int[width + 2];
        int yFoodEnergy[] = new int[width + 2];

        int xTotalAutomata[] = new int[width + 2];
        int yTotalAutomata[] = new int[width + 2];

        int xPlayerEnergy[][] = new int[numPlayers][width + 2];
        int yPlayerEnergy[][] = new int[numPlayers][width + 2];

        int xPlayerCount[][] = new int[numPlayers][width + 2];
        int yPlayerCount[][] = new int[numPlayers][width + 2];

        // --------------------------------------------------------------
        // first, graph available food energy
        int maxFoodEnergy = 0;
        int maxAutomata = 0;

        //	System.out.println("grphing avalible food");

        for (int i = 0; i < rounds.size(); i++) {
            Round thisRound = (Round) rounds.elementAt(i);

            if (thisRound.foodEnergyAvailable > maxFoodEnergy)
                maxFoodEnergy = thisRound.foodEnergyAvailable;

            if (thisRound.totalAutomata > maxAutomata)
                maxAutomata = thisRound.totalAutomata;
        }

        for (int i = 0; i < width + 2; i++) {
            xFoodEnergy[i] = i;
            xTotalAutomata[i] = i;
            if (i == 0 || i == width + 1) {
                // for edges, set to 0
                yFoodEnergy[i] = 0;
                yTotalAutomata[i] = 0;

                for (int j = 0; j < numPlayers; j++) {
                    yPlayerEnergy[j][i] = 0;
                    xPlayerEnergy[j][i] = i;

                    yPlayerCount[j][i] = 0;
                    xPlayerCount[j][i] = i;
                }
            } else {
                Round thisRound = (Round) rounds.elementAt(
                    (i - 1) * rounds.size() / width);

                yFoodEnergy[i] = (int) (((float) -thisRound.foodEnergyAvailable
                    * ((float) height / 4)) / (float) maxFoodEnergy);

                yTotalAutomata[i] = (int) (((float) -thisRound.totalAutomata
                    * ((float) height / 4)) / (float) maxAutomata);

                int totalPlayerEnergy = 0;  // total energy of all players
                // for this round

                int totalPlayerCount = 0;  // total automata count of
                // all players for this
                // round

                int previousEnergyThisRound = 0;  // how much energy
                // we've already
                // drawn

                int previousAutomataThisRound = 0;  // how many automata
                // we've already
                // drawn
                for (int j = 0; j < thisRound.players.length; j++) {
                    totalPlayerEnergy += thisRound.players[j].energy;
                    totalPlayerCount += thisRound.players[j].count;
                }

                for (int j = 0; j < thisRound.players.length; j++) {
                    float energy = (float) thisRound.players[j].energy;
                    float count = (float) thisRound.players[j].count;

                    xPlayerEnergy[j][i] = i;
                    yPlayerEnergy[j][i] = (int) (
                        -(energy + previousEnergyThisRound)
                            * ((float) height / 4) / (float) totalPlayerEnergy);
                    previousEnergyThisRound += energy;

                    xPlayerCount[j][i] = i;
                    yPlayerCount[j][i] = (int) (
                        -(count + previousAutomataThisRound)
                            * ((float) height / 4) / (float) totalPlayerCount);
                    previousAutomataThisRound += count;
                }


                // 		System.out.println("round="+
                // 				   (i-1)*rounds.size()/width+
                // 				   ", energy="+
                // 				   thisRound.foodEnergyAvailable);

                // 		System.out.println("point: ("+
                // 				   i+", "+
                // 				   yPoints[i]+")");
            }
        }

        // Create 2D by casting g to graphics2D
        Graphics2D g2d = (Graphics2D) g;

        Polygon p = new Polygon(xFoodEnergy, yFoodEnergy,
            xFoodEnergy.length);
        g2d.setColor(new Color(.7f, 1f, .8f));
        g2d.translate(0, height);
        g2d.fill(p);

        // --------------------------------------------------------------
        // next, graph each player energy

        g2d.translate(0, -((float) height / 3));
        for (int i = numPlayers - 1;
             i >= 0;
             i--) {
            //  	    for (int j = 0; j < xPlayerEnergy[i].length; j++)
            //  	    {
            //  		System.out.print(i+":"+xPlayerEnergy[i][j]+","+
            //  				 yPlayerEnergy[i][j]+" ");
            //  	    }
            p = new Polygon(xPlayerEnergy[i], yPlayerEnergy[i],
                xPlayerEnergy[i].length);
            g2d.setColor(playerColors[i]);
            g2d.fill(p);
        }


        // --------------------------------------------------------------
        // next, graph each player count

        g2d.translate(0, -((float) height / 3));
        for (int i = numPlayers - 1;
             i >= 0;
             i--) {
            p = new Polygon(xPlayerCount[i], yPlayerCount[i],
                xPlayerCount[i].length);
            g2d.setColor(playerColors[i]);
            g2d.fill(p);
        }

    }
}