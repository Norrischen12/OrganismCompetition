package organisms;

import organisms.ui.OrganismsGame;

import java.awt.Color;
import java.io.Serializable;

/**
 * Extends Constants so that all players that implement this interface will have
 * automatic access to those values/enums. Extends Serializable for...I don't
 * know, legacy reasons? TBD
 */
public interface OrganismsPlayer extends Constants, Serializable {

    /**
     * Handles information about the ongoing game and the genetic info passed
     * down from the parent
     *
     * @param game The {@link OrganismsGame} representing the current game
     * @param dna The integer passed from the parent to this organism
     * @throws Exception if something goes wrong, I guess?
     */
    void register(OrganismsGame game, int dna) throws Exception;

    /**
     * @return The species name in string form, which ideally starts with your
     *     group number
     */
    String name();

    /**
     * @return The color corresponding to this species
     */
    Color color();

    /**
     * @return whether this player should use the interactive version of the GUI
     *     (I think)
     */
    default boolean interactive() {
        return false;
    }

    /**
     * Calculates what this organism will do at the current time step. - Exact
     * food quantity at the current square is given. - Food existence is given
     * as a boolean for squares in the four cardinal directions. - Presence of a
     * neighboring organism in a given direction is indicated by an integer (the
     * other organism's {@link #externalState()} if it exists, or -1 if no
     * organism is present)
     *
     * @param foodHere The amount of food at the organism's current square
     * @param energyLeft The amount of energy this organism has stored
     * @param foodN Whether there is neighboring food to the north
     * @param foodE Whether there is neighboring food to the east
     * @param foodS Whether there is neighboring food to the south
     * @param foodW Whether there is neighboring food to the west
     * @param neighborN External state of the organism to the north, or -1
     *     if it doesn't exist
     * @param neighborE External state of the organism to the east, or -1 if
     *     it doesn't exist
     * @param neighborS External state of the organism to the south, or -1
     *     if it doesn't exist
     * @param neighborW External state of the organism to the west, or -1 if
     *     it doesn't exist
     * @return A {@link Move} object representing what this organism wants to do
     *     at this time
     * @throws Exception if something goes wrong :/
     */
    Move move(int foodHere, int energyLeft,
              boolean foodN, boolean foodE, boolean foodS, boolean foodW,
              int neighborN, int neighborE, int neighborS,
              int neighborW) throws Exception;

    /**
     * Compute a new externally displayed state for this organism. Bounded
     * between {@link #MIN_EXTERNAL_STATE} and {@link #MAX_EXTERNAL_STATE}
     * (0-255 inclusive by default)
     *
     * @return The new external state for this organism, with the bounds
     *     described above
     * @throws Exception if something goes wrong
     */
    int externalState() throws Exception;
}

