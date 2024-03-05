//***********************************************************
//*
//* File:           GameRecord.java
//* Author:         Abhinav Kamra
//* Contact:        kamra-at-cs.columbia.edu
//* Update:         9.13.2003
//*
//* Description:    Individual game setup and result storage
//*                 objects for game model tournaments. A
//*                 tournament specifies multiple game 
//*                 record entries which are then played
//*                 by the model.
//*
//***********************************************************

package organisms.ui;

public final class GameRecord implements IFCGameRecord {

    Class[] _players;
    double[] _scores;
    boolean _batchcomplete;
    
    public void setPlayers(Class[] __players) {
        int _MAX = __players.length;
        
        _players = new Class[_MAX];
        System.arraycopy(__players, 0, _players, 0, _MAX);
    }
    
    public Class[] players() {
        int _MAX = _players.length;
        Class[] RET = new Class[_MAX];
    
        System.arraycopy(_players, 0, RET, 0, _MAX);
        return RET;
    }
    
    public void setScores(double[] __scores) {
        int _MAX = __scores.length;
        
        _scores = new double[_MAX];
        System.arraycopy(__scores, 0, _scores, 0, _MAX);
    }   
    
    public double[] scores() {
        int _MAX = _scores.length;
        double[] RET = new double[_MAX];

        System.arraycopy(_scores, 0, RET, 0, _MAX);
        return RET;
    }
    
    public void setBatchComplete(boolean __batchcomplete) {
        _batchcomplete = __batchcomplete;
    }
    
    public boolean batchComplete() {
        return _batchcomplete;
    }
}

