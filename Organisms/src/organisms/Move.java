//***********************************************************
//*
//* File:           Movejava
//* Author:         Abhinav Kamra
//* Contact:        kamra-at-cs.columbia.edu
//* Update:         11.9.2003
//*
//* Description:    Basic Move object representing individaul
//*                 transformations of a polygon for Project
//*                 4, CS4444 Fall 2003.
//*
//***********************************************************

package organisms;

import java.io.Serializable;

public class Move implements Constants, Serializable {
    Action action;
    Action childPosition;
    int childKey;

    /**
     * Move the organism in the given direction.
     *
     * @param action The action taken by the organism
     * @return A {@link Move} object containing the relevant movement direction
     * @throws IllegalArgumentException if {@link Action#REPRODUCE} is
     *     given, since this is the wrong method for that
     */
    public static Move movement(Action action) {
        if (action == Action.REPRODUCE) {
            throw new IllegalArgumentException("Single-argument Move " +
                "constructor not valid for reproduction");
        }
        return new Move(action);
    }

    /**
     * Reproduce by splitting off a child in the given direction, passing along
     * one integer of information in the process
     *
     * @param childPosition The direction in which the child should be
     *     created
     * @param childKey The integer to be passed on to the child
     * @return A {@link Move} object containing reproduction information
     * @throws IllegalArgumentException if an invalid direction (not
     *     N/S/E/W) is given for {@param childPosition}
     */
    public static Move reproduce(Action childPosition, int childKey) {
        if (childPosition == Action.STAY_PUT ||
            childPosition == Action.REPRODUCE) {
            throw new IllegalArgumentException("Child position must be " +
                "one of the four cardinal directions enumerated in Action.");
        }
        return new Move(childPosition, childKey);
    }

    /**
     * What the organism wants to do this round. This constructor is
     * intentionally not public; use {@link #movement} for external
     * instantiations of a movement {@link Move}.
     *
     * @param action The action to be attempted by the organism
     * @throws IllegalArgumentException if a non-direction {@link Action} is
     *     given, since this is the wrong constructor for that
     */
    private Move(Action action) {
        if (action == Action.REPRODUCE) {
            throw new IllegalArgumentException("Single-argument Move " +
                "constructor not valid for reproduction");
        }
        this.action = action;
    }

    /**
     * A constructor for {@link Action#REPRODUCE}, requiring all relevant child
     * information to be supplied. This constructor is intentionally not public;
     * use {@link #reproduce} for external instantiations of a reproduction
     * {@link Move}.
     *
     * @param childPosition The {@link Action} corresponding to where the
     *     child should be placed
     * @param childKey The integer being passed to the child's {@link
     *     OrganismsPlayer#register} method
     */
    private Move(Action childPosition, int childKey) {
        this.action = Action.REPRODUCE;
        this.childPosition = childPosition;
        this.childKey = childKey;
    }

    public Action getAction() {
        return this.action;
    }

    public void setAction(Action t) {
        action = t;
    }

    public Action getChildPosition() {
        return this.childPosition;
    }

    public int getChildKey() {
        return childKey;
    }

    public void setChildKey(int k) {
        childKey = k;
    }

    public String toString() {
        try {
            return switch (getAction()) {
                case STAY_PUT -> "Stay Put";
                case WEST -> "Moving West";
                case EAST -> "Moving East";
                case NORTH -> "Moving North";
                case SOUTH -> "Moving South";
                case REPRODUCE -> "Reproducing";
            };

        } catch (Exception e) {
            e.printStackTrace();
            return "Error in Move.toString()";
        }
    }
}
