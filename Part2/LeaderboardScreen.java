import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

/**
 * Leaderboard screen showing cumulative points, wins, and earned badges
 * across all races played in this session.
 */
public class LeaderboardScreen extends JPanel
{
    private static final Color BG     = new Color(15, 15, 25);
    private static final Color CARD   = new Color(25, 25, 40);
    private static final Color ACCENT = new Color(82, 200, 255);
    private static final Color TEXT   = new Color(220, 220, 235);
    private static final Color MUTED  = new Color(120, 120, 145);
    private static final Color GOLD   = new Color(255, 209, 102);
    private static final Color SILVER = new Color(192, 192, 210);
    private static final Color BRONZE = new Color(205, 127, 50);

    private final GameState state;
    private final Runnable  onNewRace;

    public LeaderboardScreen(GameState state, Runnable onNewRace)
    {
        this.state     = state;
        this.onNewRace = onNewRace;
        setBackground(BG);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI()
    {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);
        header.setBorder(BorderFactory.createEmptyBorder(28, 40, 8, 40));
        header.add(label("LEADERBOARD", 28, ACCENT, Font.BOLD), BorderLayout.WEST);
        header.add(label("Cumulative points across all races", 13, MUTED, Font.PLAIN), BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Points algorithm explanation
        JPanel infoBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        infoBar.setBackground(new Color(20, 20, 35));
        infoBar.setBorder(BorderFactory.createEmptyBorder(8, 40, 8, 40));
        infoBar.add(label("Points: 3pts (1st)  2pts (2nd)  1pt (3rd)  −1pt per burnout  |  Badges: Speed Demon (3 wins in a row)  •  Iron Fingers (5 races no burnout)", 11, MUTED, Font.PLAIN));
        add(infoBar, BorderLayout.AFTER_LINE_ENDS);

        // Leaderboard entries
        JPanel content = new JPanel();
        content.setBackground(BG);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(16, 40, 20, 40));

        List<GameState.LeaderboardEntry> lb = state.leaderboard;

        if (lb.isEmpty())
        {
            JLabel empty = label("No races completed yet.", 14, MUTED, Font.PLAIN);
            empty.setAlignmentX(CENTER_ALIGNMENT);
            content.add(Box.createVerticalStrut(40));
            content.add(empty);
        }
        else
        {
            // Column header
            content.add(buildHeaderRow());
            content.add(Box.createVerticalStrut(6));

            for (int i = 0; i < lb.size(); i++)
            {
                content.add(buildEntryRow(i + 1, lb.get(i)));
                content.add(Box.createVerticalStrut(8));
            }
        }

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);

        // Wrap infoBar + scroll together
        JPanel centre = new JPanel(new BorderLayout());
        centre.setBackground(BG);
        centre.add(infoBar, BorderLayout.NORTH);
        centre.add(scroll, BorderLayout.CENTER);
        add(centre, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 16));
        footer.setBackground(BG);

        JButton newRace = makeButton("NEW RACE", new Color(82, 200, 255), new Color(10, 10, 20));
        newRace.addActionListener(e -> onNewRace.run());
        footer.add(newRace);

        add(footer, BorderLayout.SOUTH);
    }

    private JPanel buildHeaderRow()
    {
        JPanel row = new JPanel(new GridBagLayout());
        row.setBackground(BG);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.setAlignmentX(LEFT_ALIGNMENT);

        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(0, 0, 0, 0);

        gc.gridx = 0; gc.weightx = 0.05; row.add(label("RANK", 10, MUTED, Font.BOLD), gc);
        gc.gridx = 1; gc.weightx = 0.35; row.add(label("NAME", 10, MUTED, Font.BOLD), gc);
        gc.gridx = 2; gc.weightx = 0.15; row.add(label("POINTS", 10, MUTED, Font.BOLD), gc);
        gc.gridx = 3; gc.weightx = 0.15; row.add(label("WINS", 10, MUTED, Font.BOLD), gc);
        gc.gridx = 4; gc.weightx = 0.30; row.add(label("BADGE", 10, MUTED, Font.BOLD), gc);

        return row;
    }

    private JPanel buildEntryRow(int rank, GameState.LeaderboardEntry entry)
    {
        JPanel row = new JPanel(new GridBagLayout());
        boolean isTop3 = rank <= 3;
        row.setBackground(isTop3 ? new Color(28, 28, 42) : CARD);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(
                rank == 1 ? new Color(80, 75, 20)
              : rank == 2 ? new Color(60, 60, 75)
              : new Color(50, 50, 65), 1),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        row.setAlignmentX(LEFT_ALIGNMENT);

        Color rankColor = rank == 1 ? GOLD : rank == 2 ? SILVER : rank == 3 ? BRONZE : MUTED;
        String rankStr  = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : "  " + rank;

        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(0, 0, 0, 0);
        gc.fill   = GridBagConstraints.HORIZONTAL;

        gc.gridx = 0; gc.weightx = 0.05;
        row.add(label(rankStr, 18, rankColor, Font.PLAIN), gc);

        gc.gridx = 1; gc.weightx = 0.35;
        JPanel nameCol = new JPanel(new BorderLayout());
        nameCol.setBackground(row.getBackground());
        nameCol.add(label(entry.name, 14, TEXT, Font.BOLD), BorderLayout.NORTH);
        row.add(nameCol, gc);

        gc.gridx = 2; gc.weightx = 0.15;
        row.add(label(entry.totalPoints + " pts", 14, ACCENT, Font.BOLD), gc);

        gc.gridx = 3; gc.weightx = 0.15;
        row.add(label(entry.wins + " wins", 13, MUTED, Font.PLAIN), gc);

        gc.gridx = 4; gc.weightx = 0.30;
        Color badgeColor = entry.badge.equals("Speed Demon") ? GOLD
                         : entry.badge.equals("Iron Fingers") ? new Color(95, 219, 149)
                         : entry.badge.equals("Winner") ? ACCENT
                         : MUTED;
        String badgeText = entry.badge.isEmpty() ? "—" : "⭐ " + entry.badge;
        row.add(label(badgeText, 12, badgeColor, Font.BOLD), gc);

        return row;
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
