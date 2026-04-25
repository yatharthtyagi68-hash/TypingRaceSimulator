import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

/**
 * Results screen shown after a race. Displays WPM, accuracy %, burnout count,
 * and accuracy change for each typist.
 */
public class ResultsScreen extends JPanel
{
    private static final Color BG     = new Color(15, 15, 25);
    private static final Color CARD   = new Color(25, 25, 40);
    private static final Color ACCENT = new Color(82, 200, 255);
    private static final Color TEXT   = new Color(220, 220, 235);
    private static final Color MUTED  = new Color(120, 120, 145);
    private static final Color GOLD   = new Color(255, 209, 102);
    private static final Color DONE   = new Color(95, 219, 149);

    private static final String[] MEDALS = {"🥇","🥈","🥉","  4","  5","  6"};

    private final GameState state;
    private final Runnable  onLeaderboard;
    private final Runnable  onRestart;

    public ResultsScreen(GameState state, Runnable onLeaderboard, Runnable onRestart)
    {
        this.state         = state;
        this.onLeaderboard = onLeaderboard;
        this.onRestart     = onRestart;
        setBackground(BG);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI()
    {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);
        header.setBorder(BorderFactory.createEmptyBorder(28, 40, 16, 40));
        header.add(label("RACE RESULTS", 28, ACCENT, Font.BOLD), BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Results table
        JPanel content = new JPanel();
        content.setBackground(BG);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(0, 40, 20, 40));

        List<GameState.RaceResult> results = state.lastResults;

        // Sort by position
        results.sort((a, b) -> a.position - b.position);

        for (GameState.RaceResult r : results)
        {
            content.add(buildResultCard(r));
            content.add(Box.createVerticalStrut(10));
        }

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        add(scroll, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 16));
        footer.setBackground(BG);

        JButton lbBtn = makeButton("LEADERBOARD →", new Color(82, 200, 255), new Color(10, 10, 20));
        lbBtn.addActionListener(e -> onLeaderboard.run());
        footer.add(lbBtn);

        JButton restartBtn = makeButton("RACE AGAIN", new Color(50, 50, 75), TEXT);
        restartBtn.addActionListener(e -> onRestart.run());
        footer.add(restartBtn);

        add(footer, BorderLayout.SOUTH);
    }

    private JPanel buildResultCard(GameState.RaceResult r)
    {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(
                r.position == 1 ? new Color(80, 80, 30) : new Color(60, 60, 90), 1),
            BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        card.setAlignmentX(LEFT_ALIGNMENT);

        if (r.position == 1) card.setBackground(new Color(30, 30, 20));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0, 0, 0, 20);
        gc.anchor = GridBagConstraints.WEST;

        // Medal / position
        gc.gridx = 0; gc.gridy = 0; gc.gridheight = 2;
        String medal = r.position <= MEDALS.length ? MEDALS[r.position - 1] : String.valueOf(r.position);
        JLabel medLabel = label(medal, 22, r.position == 1 ? GOLD : MUTED, Font.PLAIN);
        card.add(medLabel, gc);
        gc.gridheight = 1;

        // Name
        gc.gridx = 1; gc.gridy = 0;
        card.add(label(r.name, 15, TEXT, Font.BOLD), gc);

        // Points earned
        gc.gridx = 1; gc.gridy = 1;
        card.add(label("+" + r.pointsEarned + " pts", 11, DONE, Font.PLAIN), gc);

        // Stats
        gc.gridx = 2; gc.gridy = 0;
        card.add(label(String.format("%.1f WPM", r.wpm), 13, ACCENT, Font.BOLD), gc);

        gc.gridx = 2; gc.gridy = 1;
        card.add(label(String.format("%.0f%% accuracy", r.accuracyPct), 11, MUTED, Font.PLAIN), gc);

        gc.gridx = 3; gc.gridy = 0;
        card.add(label("Burnouts: " + r.burnoutCount, 12, r.burnoutCount > 0 ? new Color(255, 107, 107) : MUTED, Font.PLAIN), gc);

        gc.gridx = 3; gc.gridy = 1;
        String accChange = r.accuracyAfter > r.accuracyBefore
            ? String.format("Acc: %.2f → %.2f ↑", r.accuracyBefore, r.accuracyAfter)
            : String.format("Acc: %.2f", r.accuracyBefore);
        card.add(label(accChange, 11, MUTED, Font.PLAIN), gc);

        return card;
    }

    private JLabel label(String text, int size, Color color, int style)
    {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Monospaced", style, size));
        l.setForeground(color);
        return l;
    }

    private JButton makeButton(String text, Color bg, Color fg)
    {
        JButton b = new JButton(text);
        b.setFont(new Font("Monospaced", Font.BOLD, 14));
        b.setBackground(bg);
        b.setForeground(fg);
        b.setBorder(BorderFactory.createEmptyBorder(12, 28, 12, 28));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
