import java.util.ArrayList;
import java.util.List;

/**
 * Holds all shared state across screens: typist configs, difficulty settings,
 * race history, and leaderboard data.
 */
public class GameState
{
    // Difficulty modifiers
    public boolean autocorrectOn  = false;
    public boolean caffeineMode   = false;
    public boolean nightShift     = false;

    // Configured typists for the current race
    public List<TypistSetup> typistSetups = new ArrayList<>();

    // Leaderboard: cumulative points per typist name
    public List<LeaderboardEntry> leaderboard = new ArrayList<>();

    // Selected passage
    public String passageText   = "";
    public int    passageLength = 0;

    // Race result from last race (for results screen)
    public List<RaceResult> lastResults = new ArrayList<>();

    // -----------------------------------------------------------------------
    // Inner classes for structured data
    // -----------------------------------------------------------------------

    public static class TypistSetup
    {
        public String name;
        public char   symbol;
        public String typingStyle;   // Touch Typist, Hunt & Peck, Phone Thumbs, Voice-to-Text
        public String keyboardType;  // Mechanical, Membrane, Touchscreen, Stenography
        public String color;         // hex string for lane color
        public boolean wristSupport;
        public boolean energyDrink;
        public boolean headphones;
        public double  baseAccuracy;

        public TypistSetup(String name, char symbol)
        {
            this.name        = name;
            this.symbol      = symbol;
            this.typingStyle = "Touch Typist";
            this.keyboardType = "Mechanical";
            this.color       = "#4A90D9";
            this.wristSupport = false;
            this.energyDrink  = false;
            this.headphones   = false;
            this.baseAccuracy = 0.70;
        }

        /**
         * Computes final accuracy after applying style, keyboard and accessory modifiers.
         */
        public double computeAccuracy()
        {
            double acc = baseAccuracy;

            // Typing style modifiers
            switch (typingStyle)
            {
                case "Touch Typist":   acc += 0.10; break;
                case "Hunt & Peck":    acc -= 0.10; break;
                case "Phone Thumbs":   acc -= 0.05; break;
                case "Voice-to-Text":  acc -= 0.15; break;
            }

            // Keyboard modifiers
            switch (keyboardType)
            {
                case "Mechanical":    acc += 0.05; break;
                case "Membrane":      acc += 0.00; break;
                case "Touchscreen":   acc -= 0.10; break;
                case "Stenography":   acc += 0.15; break;
            }

            // Accessory modifiers
            if (headphones)   acc += 0.05;
            if (wristSupport) acc += 0.02;
            // energy drink handled dynamically during race

            // Clamp
            if (acc < 0.05) acc = 0.05;
            if (acc > 0.98) acc = 0.98;
            return acc;
        }
    }

    public static class RaceResult
    {
        public String name;
        public int    position;       // 1st, 2nd, 3rd...
        public double wpm;
        public double accuracyPct;    // % of turns without a mistype
        public int    burnoutCount;
        public double accuracyBefore;
        public double accuracyAfter;
        public int    pointsEarned;

        public RaceResult(String name, int position, double wpm,
                          double accuracyPct, int burnoutCount,
                          double accuracyBefore, double accuracyAfter)
        {
            this.name           = name;
            this.position       = position;
            this.wpm            = wpm;
            this.accuracyPct    = accuracyPct;
            this.burnoutCount   = burnoutCount;
            this.accuracyBefore = accuracyBefore;
            this.accuracyAfter  = accuracyAfter;

            // Points: 3 for 1st, 2 for 2nd, 1 for 3rd, minus burnout penalty
            this.pointsEarned = Math.max(0, (4 - position) - burnoutCount);
        }
    }

    public static class LeaderboardEntry
    {
        public String name;
        public int    totalPoints;
        public int    wins;
        public int    racesWithoutBurnout;
        public int    consecutiveWins;
        public String badge = "";

        public LeaderboardEntry(String name)
        {
            this.name  = name;
            this.totalPoints = 0;
            this.wins  = 0;
            this.racesWithoutBurnout = 0;
            this.consecutiveWins = 0;
        }

        public void updateBadge()
        {
            if (consecutiveWins >= 3)       badge = "Speed Demon";
            else if (racesWithoutBurnout >= 5) badge = "Iron Fingers";
            else if (wins >= 1)             badge = "Winner";
            else                            badge = "";
        }
    }

    // -----------------------------------------------------------------------
    // Helper methods
    // -----------------------------------------------------------------------

    /**
     * Updates the leaderboard with the results of the last race.
     */
    public void applyRaceResults(List<RaceResult> results)
    {
        lastResults = results;
        for (RaceResult r : results)
        {
            LeaderboardEntry entry = findOrCreateEntry(r.name);
            entry.totalPoints += r.pointsEarned;
            if (r.position == 1)
            {
                entry.wins++;
                entry.consecutiveWins++;
            }
            else
            {
                entry.consecutiveWins = 0;
            }
            if (r.burnoutCount == 0) entry.racesWithoutBurnout++;
            else                     entry.racesWithoutBurnout = 0;
            entry.updateBadge();
        }
        // Sort by total points descending
        leaderboard.sort((a, b) -> b.totalPoints - a.totalPoints);
    }

    private LeaderboardEntry findOrCreateEntry(String name)
    {
        for (LeaderboardEntry e : leaderboard)
        {
            if (e.name.equals(name)) return e;
        }
        LeaderboardEntry e = new LeaderboardEntry(name);
        leaderboard.add(e);
        return e;
    }
}
