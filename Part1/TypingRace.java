import java.util.concurrent.TimeUnit;
import java.lang.Math;

/**
 * A typing race simulation. Three typists race to complete a passage of text,
 * advancing character by character — or sliding backwards when they mistype.
 *
 * Originally written by Ty Posaurus, who left this project to "focus on his
 * two-finger technique". He assured us the code was "basically done".
 * We have found evidence to the contrary.
 *
 * @author TyPosaurus (fixed by your name)
 * @version 1.0
 */
public class TypingRace
{
    private int passageLength;   // Total characters in the passage to type
    private Typist seat1Typist;
    private Typist seat2Typist;
    private Typist seat3Typist;

    // Accuracy thresholds for mistype and burnout events
    private static final double MISTYPE_BASE_CHANCE = 0.3;
    private static final int    SLIDE_BACK_AMOUNT   = 2;
    private static final int    BURNOUT_DURATION    = 3;

    /**
     * Constructor for objects of class TypingRace.
     * Sets up the race with a passage of the given length.
     * Initially there are no typists seated.
     *
     * @param passageLength the number of characters in the passage to type
     */
    public TypingRace(int passageLength)
    {
        this.passageLength = passageLength;
        seat1Typist = null;
        seat2Typist = null;
        seat3Typist = null;
    }

    /**
     * Seats a typist at the given seat number (1, 2, or 3).
     *
     * @param theTypist  the typist to seat
     * @param seatNumber the seat to place them in (1–3)
     */
    public void addTypist(Typist theTypist, int seatNumber)
    {
        if (seatNumber == 1)
        {
            seat1Typist = theTypist;
        }
        else if (seatNumber == 2)
        {
            seat2Typist = theTypist;
        }
        else if (seatNumber == 3)
        {
            seat3Typist = theTypist;
        }
        else
        {
            System.out.println("Cannot seat typist at seat " + seatNumber + " — there is no such seat.");
        }
    }

    /**
     * Starts the typing race.
     * All typists are reset to the beginning, then the simulation runs
     * turn by turn until one typist completes the full passage.
     * The winner's name is printed at the end.
     */
    public void startRace()
    {
        boolean finished = false;

        // BUG FIX 1: seat3Typist.resetToStart() was missing — only seats 1 and 2
        // were reset, so seat 3 could carry over state from a previous race.
        // Null checks added as a robustness improvement: if a seat was never
        // filled via addTypist(), calling methods on null causes a NullPointerException.
        if (seat1Typist != null) { seat1Typist.resetToStart(); }
        if (seat2Typist != null) { seat2Typist.resetToStart(); }
        if (seat3Typist != null) { seat3Typist.resetToStart(); }

        while (!finished)
        {
            // Advance each typist by one turn (null check in case a seat is empty)
            if (seat1Typist != null) { advanceTypist(seat1Typist); }
            if (seat2Typist != null) { advanceTypist(seat2Typist); }
            if (seat3Typist != null) { advanceTypist(seat3Typist); }

            // Print the current state of the race
            printRace();

            // Check if any typist has finished the passage
            if ( (seat1Typist != null && raceFinishedBy(seat1Typist)) ||
                 (seat2Typist != null && raceFinishedBy(seat2Typist)) ||
                 (seat3Typist != null && raceFinishedBy(seat3Typist)) )
            {
                finished = true;
            }

            // Wait 200ms between turns so the animation is visible
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (Exception e) {}
        }

        // BUG FIX 2: Winner announcement was missing entirely 
        // Determine which typist finished and print their name.
        // Also slightly increase the winner's accuracy as a reward.
        printWinner();
    }

    /**
     * Determines the winner and prints their name and updated accuracy.
     * The winner is the first typist whose progress meets or exceeds the
     * passage length. Their accuracy is slightly increased as a reward.
     */
    private void printWinner()
    {
        Typist winner = null;

        if (seat1Typist != null && raceFinishedBy(seat1Typist))
        {
            winner = seat1Typist;
        }
        else if (seat2Typist != null && raceFinishedBy(seat2Typist))
        {
            winner = seat2Typist;
        }
        else if (seat3Typist != null && raceFinishedBy(seat3Typist))
        {
            winner = seat3Typist;
        }

        if (winner != null)
        {
            double oldAccuracy = winner.getAccuracy();
            winner.setAccuracy(oldAccuracy + 0.02);
            System.out.println();
            System.out.println("  And the winner is... " + winner.getName() + "!");
            System.out.println("  Final accuracy: " + winner.getAccuracy()
                + " (improved from " + oldAccuracy + ")");
        }
    }

    /**
     * Simulates one turn for a typist.
     *
     * If the typist is burnt out, they recover one turn's worth and skip typing.
     * Otherwise:
     *   - They may type a character (advancing progress) based on their accuracy.
     *   - They may mistype (sliding back) — the chance of a mistype decreases
     *     for more accurate typists.
     *   - They may burn out — more likely for very high-accuracy typists
     *     who are pushing themselves too hard.
     *
     * @param theTypist the typist to advance
     */
    private void advanceTypist(Typist theTypist)
    {
        if (theTypist.isBurntOut())
        {
            // Recovering from burnout — skip this turn
            theTypist.recoverFromBurnout();
            return;
        }

        // Attempt to type a character
        if (Math.random() < theTypist.getAccuracy())
        {
            theTypist.typeCharacter();
        }

        // BUG FIX 3: Mistype probability was multiplied BY accuracy, meaning more
        // accurate typists mistyped MORE often — the opposite of correct behaviour.
        // Fixed: use (1 - accuracy) so higher accuracy means fewer mistypes.
        if (Math.random() < (1.0 - theTypist.getAccuracy()) * MISTYPE_BASE_CHANCE)
        {
            theTypist.slideBack(SLIDE_BACK_AMOUNT);
        }

        // Burnout check — pushing too hard increases burnout risk
        // (probability scales with accuracy squared, capped at ~0.05)
        if (Math.random() < 0.05 * theTypist.getAccuracy() * theTypist.getAccuracy())
        {
            theTypist.burnOut(BURNOUT_DURATION);
        }
    }

    /**
     * Returns true if the given typist has completed the full passage.
     *
     * @param theTypist the typist to check
     * @return true if their progress has reached or passed the passage length
     */
    private boolean raceFinishedBy(Typist theTypist)
    {
        // BUG FIX 4: Original used == which fails if progress overshoots
        // passageLength (e.g. progress becomes 41 in a 40-char race).
        // Fixed: use >= so any progress at or beyond the end counts as finished.
        return theTypist.getProgress() >= passageLength;
    }

    /**
     * Prints the current state of the race to the terminal.
     * Shows each typist's position along the passage and burnout state.
     */
    private void printRace()
    {
        System.out.print('\u000C'); // Clear terminal

        System.out.println("  TYPING RACE — passage length: " + passageLength + " chars");
        multiplePrint('=', passageLength + 3);
        System.out.println();

        printSeat(seat1Typist);
        System.out.println();

        printSeat(seat2Typist);
        System.out.println();

        printSeat(seat3Typist);
        System.out.println();

        multiplePrint('=', passageLength + 3);
        System.out.println();
        System.out.println("  [~] = burnt out    [<] = just mistyped");
    }

    /**
     * Prints a single typist's lane.
     *
     * @param theTypist the typist whose lane to print
     */
    private void printSeat(Typist theTypist)
    {
        // BUG FIX 5: spacesAfter could go negative if progress >= passageLength
        // (i.e. the typist has finished or overshot). While multiplePrint() would
        // silently skip printing with a negative value (loop condition fails immediately),
        // this still produces incorrect lane formatting — the closing | appears in the
        // wrong position. Clamped to 0 to ensure predictable, correct rendering.
        int spacesBefore = Math.min(theTypist.getProgress(), passageLength);
        int spacesAfter  = Math.max(passageLength - theTypist.getProgress(), 0);

        System.out.print('|');
        multiplePrint(' ', spacesBefore);

        System.out.print(theTypist.getSymbol());
        if (theTypist.isBurntOut())
        {
            System.out.print('~');
            spacesAfter = Math.max(spacesAfter - 1, 0); // guard against going negative
        }

        multiplePrint(' ', spacesAfter);
        System.out.print('|');
        System.out.print(' ');

        // Print name and accuracy
        if (theTypist.isBurntOut())
        {
            System.out.print(theTypist.getName()
                + " (Accuracy: " + theTypist.getAccuracy() + ")"
                + " BURNT OUT (" + theTypist.getBurnoutTurnsRemaining() + " turns)");
        }
        else
        {
            System.out.print(theTypist.getName()
                + " (Accuracy: " + theTypist.getAccuracy() + ")");
        }
    }

    /**
     * Prints a character a given number of times.
     *
     * @param aChar the character to print
     * @param times how many times to print it
     */
    private void multiplePrint(char aChar, int times)
    {
        int i = 0;
        while (i < times)
        {
            System.out.print(aChar);
            i = i + 1;
        }
    }

    /**
     * Entry point for testing the race simulation.
     */
    public static void main(String[] args)
    {
        TypingRace race = new TypingRace(40);
        race.addTypist(new Typist('①', "TURBOFINGERS", 0.85), 1);
        race.addTypist(new Typist('②', "QWERTY_QUEEN",  0.60), 2);
        race.addTypist(new Typist('③', "HUNT_N_PECK",   0.30), 3);
        race.startRace();
    }
}
