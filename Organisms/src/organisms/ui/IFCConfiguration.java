//***********************************************************
//*
//* File:           IFCConfiguration.java
//* Author:         Abhinav Kamra
//* Contact:        kamra-at-cs.columbia.edu
//* Update:         9.5.2003
//*
//* Description:    Configuration object interface.  Used to
//*                 store, specify, and retrieve
//*                 configuration parameters during
//*                 model instantiation.
//*
//***********************************************************

package organisms.ui;

import java.io.Serializable;

public interface IFCConfiguration extends Serializable {

    // Game parameters
    void setNumRounds(int __numrounds) ;

    int numRounds() ;

    void setNumRoundsBounds(int __min, int __max);

    int numRoundsMin() ;

    int numRoundsMax() ;

    int numPlayers() ;

    void setNumPlayersBounds(int __min, int __max) ;

    int numPlayersMin() ;

    int numPlayersMax() ;

    void setInitEnergy(int _init) ;

    int getInitEnergy() ;

    void setGridX(int x) ;

    int getGridX();

    void setXBounds(int x1, int x2) ;

    void setGridY(int y) ;

    int getGridY();

    void setYBounds(int y1, int y2) ;

    void setP(double p) ;

    double getP() ;

    void setPBounds(double p1, double p2) ;

    void setQ(double q) ;

    double getQ() ;

    void setQBounds(double q1, double q2) ;

    void setS(int s) ;

    int getS() ;

    void setV(int v) ;

    int getV() ;

    void setVBounds(int v1, int v2) ;

    void setU(int u) ;

    int getU() ;

    void setUBounds(int u1, int u2) ;

    void setM(int m) ;

    int M() ;

    void setMBounds(int m1, int m2) ;

    void setK(int k) ;

    int K() ;

    void setKBounds(int k1, int k2) ;

    // Class and Players
    void setClassList(Class[] __list) ;

    Class[] getClassList() ;

    Class getClass(int __pos) ;

    void setClass(int __pos, Class __class) ;

    void setPlayerList(Class[] __list) ;

    Class[] playerList() ;

    Class player(int __pos) ;

    void setPlayer(int __pos, Class __class) ;

    String logFile() ;

    void setLogFile(String __str) ;
}
