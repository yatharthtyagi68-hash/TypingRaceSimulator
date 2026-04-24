/**
 * Represents a competitor in a typing race.
 *
 * Each typist has a name, a symbol for display, an accuracy rating,
 * a progress counter, and a burnout state with a turn countdown.
 *
 * @author (your name)
 * @version 1.0
 */
public class Typist
{
    // The character used to represent this typist on screen (e.g. '①')
    private char symbol;

    // The display name of this typist (e.g. "TURBOFINGERS")
    private String name;

    // How far along the passage this typist has reached (never goes below 0)
    private int progress;

    // Whether this typist is currently burnt out
    private boolean burntOut;

    // How many turns of burnout remain (0 when not burnt out)
    private int burnoutTurnsRemaining;

    // Accuracy rating between 0.0 (very inaccurate) and 1.0 (perfect)
    private double accuracy;


    /**
     * Constructor for objects of class Typist.
     * Creates a new typist with a given symbol, name, and accuracy rating.
     *
     * @param typistSymbol   a single Unicode character representing this typist (e.g. '①')
     * @param typistName     the display name of the typist (e.g. "TURBOFINGERS")
     * @param typistAccuracy the typist's accuracy rating, between 0.0 and 1.0
     */
    public Typist(char typistSymbol, String typistName, double typistAccuracy)
    {
        symbol = typistSymbol;
        name   = typistName;

        // Use the setter so clamping is applied from the start
        setAccuracy(typistAccuracy);

        progress              = 0;
        burntOut              = false;
        burnoutTurnsRemaining = 0;
    }


    // -----------------------------------------------------------------------
    // Burnout methods
    // -----------------------------------------------------------------------

    /**
     * Sets this typist into a burnt-out state for a given number of turns.
     *
     * @param turns the number of turns the burnout will last
     */
    public void burnOut(int turns)
    {
        burntOut              = true;
        burnoutTurnsRemaining = turns;
    }

    /**
     * Reduces the remaining burnout counter by one turn.
     * When the counter reaches zero the typist is no longer burnt out.
     * Has no effect if the typist is not currently burnt out.
     */
    public void recoverFromBurnout()
    {
        if (burntOut)
        {
            burnoutTurnsRemaining--;
            if (burnoutTurnsRemaining <= 0)
            {
                burntOut              = false;
                burnoutTurnsRemaining = 0;
            }
        }
    }

    /**
     * Returns true if this typist is currently burnt out, false otherwise.
     *
     * @return true if burnt out
     */
    public boolean isBurntOut()
    {
        return burntOut;
    }

    /**
     * Returns the number of turns of burnout remaining.
     * Returns 0 if the typist is not currently burnt out.
     *
     * @return burnout turns remaining as a non-negative integer
     */
    public int getBurnoutTurnsRemaining()
    {
        return burnoutTurnsRemaining;
    }


    // -----------------------------------------------------------------------
    // Progress methods
    // -----------------------------------------------------------------------

    /**
     * Advances the typist forward by one character along the passage.
     * Should only be called when the typist is not burnt out.
     */
    public void typeCharacter()
    {
        progress++;
    }

    /**
     * Moves the typist backwards by a given number of characters (a mistype).
     * Progress cannot go below zero.
     *
     * @param amount the number of characters to slide back (must be positive)
     */
    public void slideBack(int amount)
    {
        progress -= amount;
        if (progress < 0)
        {
            progress = 0;
        }
    }

    /**
     * Resets the typist to their initial state, ready for a new race.
     * Progress returns to zero and all burnout state is cleared.
     */
    public void resetToStart()
    {
        progress              = 0;
        burntOut              = false;
        burnoutTurnsRemaining = 0;
    }

    /**
     * Returns the typist's current progress through the passage.
     *
     * @return progress as a non-negative integer
     */
    public int getProgress()
    {
        return progress;
    }


    // -----------------------------------------------------------------------
    // Getters and setters
    // -----------------------------------------------------------------------

    /**
     * Returns the typist's accuracy rating.
     *
     * @return accuracy as a double between 0.0 and 1.0
     */
    public double getAccuracy()
    {
        return accuracy;
    }

    /**
     * Sets the accuracy rating of the typist.
     * Values below 0.0 are clamped to 0.0; values above 1.0 are clamped to 1.0.
     *
     * @param newAccuracy the new accuracy rating
     */
    public void setAccuracy(double newAccuracy)
    {
        if (newAccuracy < 0.0)
        {
            accuracy = 0.0;
        }
        else if (newAccuracy > 1.0)
        {
            accuracy = 1.0;
        }
        else
        {
            accuracy = newAccuracy;
        }
    }

    /**
     * Returns the name of the typist.
     *
     * @return the typist's name as a String
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the character symbol used to represent this typist.
     *
     * @return the typist's symbol as a char
     */
    public char getSymbol()
    {
        return symbol;
    }

    /**
     * Sets the character used to represent this typist.
     *
     * @param newSymbol the new symbol character
     */
    public void setSymbol(char newSymbol)
    {
        symbol = newSymbol;
    }
}
