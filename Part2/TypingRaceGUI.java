import javax.swing.*;
import java.awt.*;

/**
 * Main entry point for the GUI typing race simulator.
 * Manages screen navigation using CardLayout.
 *
 * Start the application by calling startRaceGUI().
 *
 * @author (your name)
 * @version 1.0
 */
public class TypingRaceGUI
{
    private static final String SCREEN_CONFIG        = "config";
    private static final String SCREEN_TYPIST_CONFIG = "typistConfig";
    private static final String SCREEN_RACE          = "race";
    private static final String SCREEN_RESULTS       = "results";
    private static final String SCREEN_LEADERBOARD   = "leaderboard";

    private JFrame      window;
    private JPanel      cardPanel;
    private CardLayout  cardLayout;
    private GameState   state;

    /**
     * Launches the GUI typing race simulator.
     * This is the required entry point per the coursework spec.
     */
    public void startRaceGUI()
    {
        SwingUtilities.invokeLater(this::initAndShow);
    }

    private void initAndShow()
    {
        state      = new GameState();
        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.setBackground(new Color(15, 15, 25));

        window = new JFrame("Typing Race Simulator");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setPreferredSize(new Dimension(900, 680));
        window.setMinimumSize(new Dimension(750, 560));
        window.add(cardPanel);

        // Show config screen first
        showConfigScreen();

        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }

    // -----------------------------------------------------------------------
    // Screen transitions
    // -----------------------------------------------------------------------

    private void showConfigScreen()
    {
        cardPanel.removeAll();

        ConfigScreen config = new ConfigScreen(state, this::showTypistConfigScreen);
        cardPanel.add(config, SCREEN_CONFIG);
        cardLayout.show(cardPanel, SCREEN_CONFIG);
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    private void showTypistConfigScreen()
    {
        cardPanel.removeAll();

        TypistConfigScreen typistConfig = new TypistConfigScreen(
            state,
            this::showConfigScreen,   // back
            this::showRaceScreen      // next
        );
        cardPanel.add(typistConfig, SCREEN_TYPIST_CONFIG);
        cardLayout.show(cardPanel, SCREEN_TYPIST_CONFIG);
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    private void showRaceScreen()
    {
        cardPanel.removeAll();

        RaceScreen race = new RaceScreen(state, this::showResultsScreen);
        cardPanel.add(race, SCREEN_RACE);
        cardLayout.show(cardPanel, SCREEN_RACE);
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    private void showResultsScreen()
    {
        cardPanel.removeAll();

        ResultsScreen results = new ResultsScreen(
            state,
            this::showLeaderboardScreen,  // leaderboard button
            this::showConfigScreen        // race again button
        );
        cardPanel.add(results, SCREEN_RESULTS);
        cardLayout.show(cardPanel, SCREEN_RESULTS);
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    private void showLeaderboardScreen()
    {
        cardPanel.removeAll();

        LeaderboardScreen lb = new LeaderboardScreen(state, this::showConfigScreen);
        cardPanel.add(lb, SCREEN_LEADERBOARD);
        cardLayout.show(cardPanel, SCREEN_LEADERBOARD);
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    // -----------------------------------------------------------------------
    // Main method for direct launch
    // -----------------------------------------------------------------------

    public static void main(String[] args)
    {
        new TypingRaceGUI().startRaceGUI();
    }
}
