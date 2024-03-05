//***********************************************************
//*
//* File:           IFCGameRecord.java
//* Author:         Abhinav Kamra
//* Contact:        kamra-at-cs.columbia.edu
//* Update:         9.13.2003
//*
//* Description:    Interface for individual game entries
//*                 used to specify and store tournament
//*                 games.
//*
//***********************************************************

package organisms.ui;

import java.io.Serializable;

public interface IFCGameRecord extends Serializable {

    public void setPlayers(Class[] __players);

    public Class[] players();

    public void setScores(double[] __scores);

    public double[] scores();

    public void setBatchComplete(boolean __batchcomplete);

    public boolean batchComplete();
}
