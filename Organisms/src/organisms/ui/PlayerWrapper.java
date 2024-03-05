//***********************************************************
//*
//* File:           PlayerWrapper.java
//* Author:         Abhinav Kamra
//* Contact:        kamra-at-cs.columbia.edu
//* Update:         10.16.2003
//*
//* Description:    Compositional wrapper for IFCPlayer
//*                 objects.
//*
//***********************************************************

package organisms.ui;

import organisms.Move;
import organisms.OrganismsPlayer;
import organisms.PersistentPlayer;

import java.awt.*;
import java.io.Serializable;

import static organisms.Constants.MAX_EXTERNAL_STATE;
import static organisms.Constants.MIN_EXTERNAL_STATE;

public final class PlayerWrapper implements Serializable {

    int energy;
    int externalState;
    double score;
    OrganismsPlayer player;
    Class playerClass;
    OrganismsGame game;

    PlayerWrapper(Class __class) {
        playerClass = __class;
        this.energy = 0;
    }

    private void register_priv(OrganismsGame __amoeba, int key) {
        try {
            game = __amoeba;
            player.register(game, key);
            externalState = 0;
            selfUpdateExternalState();
        } catch (Exception EXC) {
            System.out.println(EXC.getMessage());
            EXC.printStackTrace();
            System.out.println(
                "Player " + playerClass + " threw an Exception in register()");
        }
    }

    public void register(OrganismsGame __amoeba, int key) {
        try {
            player = (OrganismsPlayer) playerClass.newInstance();
            register_priv(__amoeba, key);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public String name() {
        try {
            return player.name();
        } catch (Exception EXC) {
            System.out.println(EXC.getMessage());
            EXC.printStackTrace();
            System.out.println(
                "Player " + playerClass + " threw an Exception in name()");
            return "Anonymous";
        }
    }

    public void selfUpdateExternalState() {
        try {
            int x = player.externalState();
            if ((x >= MIN_EXTERNAL_STATE) & (x <= MAX_EXTERNAL_STATE))
                externalState = x;
        } catch (Exception EXC) {
            System.out.println(EXC.getMessage());
            EXC.printStackTrace();
            System.out.println(
                "Player " + playerClass + " threw an Exception in " +
                    "externalState()");
        }
    }

    public Color color() {
        if (player != null)
            return player.color();
        else
            return new Color(1.0f, 1.0f, 0.9f);

    }

    public Class playerClass() {
        return playerClass;
    }

    public void gameOver() {
        try {
            if (player instanceof PersistentPlayer) {
                ((PersistentPlayer) player).gameOver();
            }
        } catch (Exception EXC) {
            System.out.println(EXC.getMessage());
            EXC.printStackTrace();
            System.out.println(
                "Player " + playerClass + " threw an Exception in gameOver()");
        }
    }

    public Move move(int foodHere, boolean foodN, boolean foodE,
                     boolean foodS, boolean foodW, int neighborN, int neighborE,
                     int neighborS, int neighborW) {
        try {
            return this.player.move(foodHere, this.energy, foodN, foodE, foodS,
                foodW, neighborN, neighborE, neighborS, neighborW);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public boolean interactive() {
        try {
            return player.interactive();
        } catch (Exception EXC) {
            System.out.println(EXC.getMessage());
            EXC.printStackTrace();
            System.out.println(
                "Player " + playerClass + " threw an Exception in interactive" +
                    "()");
            return false;
        }
    }

    public double score() {
        return this.score;
    }

    public int energy() {
        return this.energy;
    }

    public int getExternalState() {
        return externalState;
    }

    public void setScore(double __score) {
        this.score = __score;
    }

    public void setEnergy(int __energy) {
        this.energy = __energy;
    }

    public OrganismsPlayer player() {
        return this.player;
    }

    public String toString() {
        return "[" + this.player.name() + "]";
    }
}
