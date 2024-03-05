//***********************************************************
//*
//* File:           Configuration.java
//* Author:         Abhinav Kamra
//* Contact:        kamra-at-cs.columbia.edu
//* Update:         9.5.2003
//*
//* Description:    Configuration object for Project 1, CS4444
//*                 Fall 2003
//*
//*
//***********************************************************

package organisms.ui;

import java.util.*;

public final class Configuration implements IFCConfiguration {
    int minRounds;
    int maxRounds;
    int minPlayers;
    int maxPlayers;
    int minX;
    int maxX;
    int minY;
    int maxY;
    int minU;
    int maxU;
    int minV;
    int maxV;
    int minM;
    int maxM;
    int minK;
    int maxK;

    double minP;
    double maxP;
    double minQ;
    double maxQ;

    int initEnergy;
    int numRounds;
    int X, Y, u, s, v, M, K;
    double p, q;
    Class[] classList;
    Class[] playerList;
    String _logfile;


    public void setInitEnergy(int _init) {
        initEnergy = _init;
    }

    public int getInitEnergy() {
        return initEnergy;
    }

    private void validateRange(int provided, int lower, int upper) {
        if (provided < lower || provided > upper) {
            throwRangeError(Integer.toString(provided), Integer.toString(lower),
                Integer.toString(upper));
        }
    }

    private void validateRange(double provided, double lower, double upper) {
        // TODO (soft): add epsilon tolerance to this check?
        if (provided < lower || provided > upper) {
            throwRangeError(Double.toString(provided), Double.toString(lower),
                Double.toString(upper));
        }
    }

    private void throwRangeError(String provided, String lower, String upper) {
        throw new IllegalArgumentException("Valid range is [" +
            lower + ", " + upper + "]; was given " + provided);
    }

    public void setNumRounds(int numRounds) {
        validateRange(numRounds, this.minRounds, this.maxRounds);
        this.numRounds = numRounds;
    }

    public int numRounds() {
        return numRounds;
    }

    public void setNumRoundsBounds(int minRounds, int maxRounds) {
        this.minRounds = minRounds;
        this.maxRounds = maxRounds;
    }

    public int numRoundsMin() {
        return this.minRounds;
    }

    public int numRoundsMax() {
        return this.maxRounds;
    }

    public int numPlayers() {
        return playerList.length;
    }

    public void setNumPlayersBounds(int minPlayers, int maxPlayers) {
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
    }

    public int numPlayersMin() {
        return this.minPlayers;
    }

    public int numPlayersMax() {
        return this.maxPlayers;
    }

    public void setGridX(int _x) {
        validateRange(_x, this.minX, this.maxX);
        X = _x;
    }

    public int getGridX() {
        return X;
    }

    public void setXBounds(int minX, int maxX) {
        this.minX = minX;
        this.maxX = maxX;
    }

    public void setGridY(int _y) {
        validateRange(_y, this.minY, maxY);
        Y = _y;
    }

    public int getGridY() {
        return Y;
    }

    public void setYBounds(int minY, int maxY) {
        this.minY = minY;
        this.maxY = maxY;
    }

    @Override
    public void setS(int s) {
        this.s = s;
    }

    @Override
    public int getS() {
        return s;
    }

    public void setV(int _v) {
        validateRange(_v, this.minV, this.maxV);
        v = _v;
    }

    public int getV() {
        return v;
    }

    public void setVBounds(int minV, int maxV) {
        this.minV = minV;
        this.maxV = maxV;
    }

    public void setU(int _u) {
        validateRange(_u, this.minU, this.maxU);
        u = _u;
    }

    public int getU() {
        return u;
    }

    public void setUBounds(int minU, int maxU) {
        this.minU = minU;
        this.maxU = maxU;
    }

    public void setM(int _m) {
        validateRange(_m, this.minM, this.maxM);
        M = _m;
    }

    public int M() {
        return M;
    }

    public void setMBounds(int minM, int maxM) {
        this.minM = minM;
        this.maxM = maxM;
    }

    public void setK(int _k) {
        validateRange(_k, this.minK, this.maxK);
        K = _k;
    }

    public int K() {
        return K;
    }

    public void setKBounds(int minK, int maxK) {
        this.minK = minK;
        this.maxK = maxK;
    }

    public void setP(double _p) {
        validateRange(_p, this.minP, this.maxP);
        p = _p;
    }

    public double getP() {
        return p;
    }

    public void setPBounds(double minP, double maxP) {
        this.minP = minP;
        this.maxP = maxP;
    }

    public void setQ(double _q) {
        validateRange(_q, this.minQ, this.maxQ);
        q = _q;
    }

    public double getQ() {
        return q;
    }

    public void setQBounds(double minQ, double maxQ) {
        this.minQ = minQ;
        this.maxQ = maxQ;
    }

    public void setClassList(Class[] __list) {
        int _MAX = __list.length;

        classList = new Class[_MAX];
        System.arraycopy(__list, 0, classList, 0, _MAX);
    }

    public Class[] getClassList() {
        int _MAX = classList.length;
        Class[] RET = new Class[_MAX];

        System.arraycopy(classList, 0, RET, 0, _MAX);
        return RET;
    }

    public Class getClass(int __pos) {
        return classList[__pos];
    }

    public void setClass(int __pos, Class __class) {
        classList[__pos] = __class;
    }

    public void setPlayerList(Class[] __list) {
        int _MAX = __list.length;

        if (_MAX < this.minPlayers || _MAX > this.maxPlayers) {
            throw new RuntimeException(
                "Error:  Number of Players Out of Range: " + _MAX);
        }
        playerList = new Class[_MAX];
        System.arraycopy(__list, 0, playerList, 0, _MAX);
    }

    public Class[] playerList() {
        int _MAX = playerList.length;
        Class[] RET = new Class[_MAX];

        System.arraycopy(playerList, 0, RET, 0, _MAX);
        return RET;
    }

    public Class[] randomPlayerList() {
        int _MAX = playerList.length;
        Class[] RET = new Class[_MAX];
        ArrayList<Integer> ar = new ArrayList<>(_MAX);
        Random rand = new Random();

        for (int i = 0; i < _MAX; i++) {
            ar.add(i);
        }
        for (int i = 0; i < _MAX; i++) {
            int index = ar.remove(rand.nextInt(ar.size()));
            RET[i] = playerList[index];
        }

        return RET;
    }

    public Class player(int __pos) {
        return playerList[__pos];
    }

    public void setPlayer(int __pos, Class __class) {
        playerList[__pos] = __class;
    }

    public String logFile() {
        return _logfile;
    }

    public void setLogFile(String __logfile) {
        _logfile = __logfile;
    }
}
