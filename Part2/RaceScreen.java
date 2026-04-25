import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Animated race screen. Shows the passage text with each typist's cursor
 * advancing character by character. Handles all difficulty modifiers.
 */
public class RaceScreen extends JPanel
{
    private static final Color BG     = new Color(15, 15, 25);
    private static final Color CARD   = new Color(25, 25, 40);
    private static final Color ACCENT = new Color(82, 200, 255);
    private static final Color TEXT   = new Color(220, 220, 235);
    private static final Color MUTED  = new Color(120, 120, 145);
    private static final Color DONE   = new Color(95, 219, 149);

    private static final double MISTYPE_BASE  = 0.3;
    private static final int    SLIDE_BACK    = 2;
    private static final int    BURNOUT_DUR   = 3;
    private static final int    TURN_DELAY_MS = 180;

    private final GameState state;
    private final Runnable  onFinish;

    // Runtime race state
    private Typist[]   typists;
    private int[]      burnoutCounts;
    private int[]      mistypeCounts;
    private boolean[]  justMistyped;
    private long       raceStartTime;
    private int        turnNumber;
    private String     winnerName;
    private boolean    raceOver;

    // UI components
    private JLabel     turnLabel;
    private JPanel[]   lanePanels;
    private JLabel[]   passageLabels;
    private JLabel[]   statusLabels;
    private JLabel     winnerLabel;
    private javax.swing.Timer timer;

    public RaceScreen(GameState state, Runnable onFinish)
    {
        this.state    = state;
        this.onFinish = onFinish;
        setBackground(BG);
        setLayout(new BorderLayout());
        initTypists();
        buildUI();
        startRace();
    }

    // -----------------------------------------------------------------------
    // Initialise Typist objects from GameState config
    // -----------------------------------------------------------------------

    private void initTypists()
    {
        int n = state.typistSetups.size();
        typists       = new Typist[n];
        burnoutCounts = new int[n];
        mistypeCounts = new int[n];
        justMistyped  = new boolean[n];

        for (int i = 0; i < n; i++)
        {
            GameState.TypistSetup ts = state.typistSetups.get(i);
            double acc = ts.computeAccuracy();
            if (state.nightShift) acc -= 0.08;
            if (acc < 0.05) acc = 0.05;
            typists[i] = new Typist(ts.symbol, ts.name, acc);
        }
    }

    // -----------------------------------------------------------------------
    // Build UI
    // -----------------------------------------------------------------------

    private void buildUI()
    {
        int n = state.typistSetups.size();

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);
        header.setBorder(BorderFactory.createEmptyBorder(20, 40, 10, 40));

        JLabel title = label("TYPING RACE", 22, ACCENT, Font.BOLD);
        header.add(title, BorderLayout.WEST);

        turnLabel = label("Turn 0", 13, MUTED, Font.PLAIN);
        header.add(turnLabel, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Race lanes
        JPanel lanesPanel = new JPanel();
        lanesPanel.setBackground(BG);
        lanesPanel.setLayout(new BoxLayout(lanesPanel, BoxLayout.Y_AXIS));
        lanesPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));

        // Passage display at top
        JPanel passageCard = new JPanel(new BorderLayout());
        passageCard.setBackground(CARD);
        passageCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 90), 1),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        JLabel passTitle = label("PASSAGE", 10, MUTED, Font.BOLD);
        passageCard.add(passTitle, BorderLayout.NORTH);
        JLabel passText = label(state.passageText, 14, TEXT, Font.PLAIN);
        passText.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        passageCard.add(passText, BorderLayout.CENTER);
        passageCard.setAlignmentX(LEFT_ALIGNMENT);
        passageCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        lanesPanel.add(passageCard);
        lanesPanel.add(Box.createVerticalStrut(16));

        // Per-typist lane panels
        lanePanels     = new JPanel[n];
        passageLabels  = new JLabel[n];
        statusLabels   = new JLabel[n];

        for (int i = 0; i < n; i++)
        {
            GameState.TypistSetup ts = state.typistSetups.get(i);
            Color laneColor = safeDecodeColor(ts.color, ACCENT);

            JPanel lane = new JPanel();
            lane.setBackground(CARD);
            lane.setLayout(new BorderLayout(10, 0));
            lane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 90), 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
            ));
            lane.setAlignmentX(LEFT_ALIGNMENT);
            lane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

            // Left: typist name + symbol
            JLabel nameLabel = label(ts.symbol + "  " + ts.name, 13, laneColor, Font.BOLD);
            nameLabel.setPreferredSize(new Dimension(180, 20));
            lane.add(nameLabel, BorderLayout.WEST);

            // Centre: passage progress display
            JLabel pLabel = buildPassageLabel(i);
            lane.add(pLabel, BorderLayout.CENTER);
            passageLabels[i] = pLabel;

            // Right: status
            JLabel sLabel = label("", 11, MUTED, Font.PLAIN);
            sLabel.setPreferredSize(new Dimension(160, 20));
            sLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            lane.add(sLabel, BorderLayout.EAST);
            statusLabels[i] = sLabel;

            lanePanels[i] = lane;
            lanesPanel.add(lane);
            lanesPanel.add(Box.createVerticalStrut(8));
        }

        // Winner label (hidden until race ends)
        winnerLabel = label("", 20, DONE, Font.BOLD);
        winnerLabel.setAlignmentX(LEFT_ALIGNMENT);
        winnerLabel.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));
        lanesPanel.add(winnerLabel);

        JScrollPane scroll = new JScrollPane(lanesPanel);
        scroll.setBorder(null);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        add(scroll, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(BG);
        footer.setBorder(BorderFactory.createEmptyBorder(8, 0, 16, 0));

        JButton viewResults = makeButton("VIEW RESULTS →", new Color(82, 200, 255), new Color(10, 10, 20));
        viewResults.setVisible(false);
        viewResults.addActionListener(e -> onFinish.run());
        footer.add(viewResults);

        // Store ref so we can show it when race ends
        this.viewResultsBtn = viewResults;
        add(footer, BorderLayout.SOUTH);
    }

    private JButton viewResultsBtn;

    private JLabel buildPassageLabel(int idx)
    {
        JLabel l = new JLabel(buildPassageHTML(idx));
        l.setFont(new Font("Monospaced", Font.PLAIN, 13));
        return l;
    }

    /**
     * Builds HTML-coloured passage: completed chars in typist colour,
     * cursor char highlighted, remaining chars in muted grey.
     */
    private String buildPassageHTML(int idx)
    {
        String passage  = state.passageText;
        int    progress = Math.min(typists[idx].getProgress(), passage.length());
        String hex      = state.typistSetups.get(idx).color;

        StringBuilder sb = new StringBuilder("<html><body style='font-family:monospace;font-size:12px'>");

        // Completed portion
        if (progress > 0)
        {
            sb.append("<span style='color:").append(hex).append(";'>")
              .append(escapeHtml(passage.substring(0, progress)))
              .append("</span>");
        }

        // Cursor
        if (progress < passage.length())
        {
            String cursorStyle = typists[idx].isBurntOut()
                ? "background:#FF6B6B;color:#111;"
                : "background:" + hex + ";color:#111;";
            sb.append("<span style='").append(cursorStyle).append("'>")
              .append(escapeHtml(String.valueOf(passage.charAt(progress))))
              .append("</span>");

            // Remaining
            if (progress + 1 < passage.length())
            {
                sb.append("<span style='color:#606080;'>")
                  .append(escapeHtml(passage.substring(progress + 1)))
                  .append("</span>");
            }
        }

        sb.append("</body></html>");
        return sb.toString();
    }

    private String escapeHtml(String s)
    {
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace(" ","&nbsp;");
    }

    // -----------------------------------------------------------------------
    // Race simulation
    // -----------------------------------------------------------------------

    private void startRace()
    {
        for (Typist t : typists) t.resetToStart();
        raceStartTime = System.currentTimeMillis();
        turnNumber    = 0;
        raceOver      = false;
        winnerName    = null;

        timer = new javax.swing.Timer(TURN_DELAY_MS, e -> doTurn());
        timer.start();
    }

    private void doTurn()
    {
        if (raceOver) return;
        turnNumber++;
        turnLabel.setText("Turn " + turnNumber);

        Arrays.fill(justMistyped, false);

        for (int i = 0; i < typists.length; i++)
        {
            advanceTypist(i);
        }

        updateDisplay();

        // Check for winner
        for (int i = 0; i < typists.length; i++)
        {
            if (typists[i].getProgress() >= state.passageLength)
            {
                raceOver   = true;
                winnerName = typists[i].getName();
                timer.stop();
                showWinner(i);
                return;
            }
        }
    }

    private void advanceTypist(int i)
    {
        Typist t = typists[i];

        if (t.isBurntOut())
        {
            t.recoverFromBurnout();
            return;
        }

        // Caffeine boost: higher accuracy in first 10 turns
        double acc = t.getAccuracy();
        if (state.caffeineMode && turnNumber <= 10) acc = Math.min(acc + 0.15, 0.99);

        // Type character
        if (Math.random() < acc) t.typeCharacter();

        // Mistype chance — reduced by headphones accessory
        double mistypeChance = (1.0 - acc) * MISTYPE_BASE;
        if (state.typistSetups.get(i).headphones) mistypeChance *= 0.5;
        if (state.autocorrectOn) mistypeChance *= 0.5;

        if (Math.random() < mistypeChance)
        {
            int slideAmount = state.autocorrectOn ? Math.max(1, SLIDE_BACK / 2) : SLIDE_BACK;
            t.slideBack(slideAmount);
            justMistyped[i] = true;
            mistypeCounts[i]++;
        }

        // Burnout check — caffeine increases risk after turn 10
        double burnoutChance = 0.05 * acc * acc;
        if (state.caffeineMode && turnNumber > 10) burnoutChance *= 2.0;
        int burnoutDuration = state.typistSetups.get(i).wristSupport
            ? Math.max(1, BURNOUT_DUR - 1) : BURNOUT_DUR;

        if (Math.random() < burnoutChance)
        {
            t.burnOut(burnoutDuration);
            burnoutCounts[i]++;
        }
    }

    private void updateDisplay()
    {
        for (int i = 0; i < typists.length; i++)
        {
            passageLabels[i].setText(buildPassageHTML(i));

            String status = "";
            if (typists[i].isBurntOut())
                status = "BURNT OUT (" + typists[i].getBurnoutTurnsRemaining() + " turns)";
            else if (justMistyped[i])
                status = "[<] mistyped";

            statusLabels[i].setText(status);

            // Highlight lane if burnt out
            lanePanels[i].setBackground(typists[i].isBurntOut()
                ? new Color(40, 20, 20) : CARD);
        }
    }

    private void showWinner(int winnerIdx)
    {
        long elapsed = System.currentTimeMillis() - raceStartTime;
        double minutes = elapsed / 60000.0;

        // Build results
        List<GameState.RaceResult> results = new ArrayList<>();
        int[] positions = computePositions();

        for (int i = 0; i < typists.length; i++)
        {
            double wpm = minutes > 0 ? (state.passageLength / 5.0) / minutes : 0;
            double accPct = turnNumber > 0
                ? (1.0 - (double) mistypeCounts[i] / turnNumber) * 100 : 100;
            double oldAcc = typists[i].getAccuracy();
            double newAcc = positions[i] == 1 ? oldAcc + 0.02 : oldAcc;
            typists[i].setAccuracy(newAcc);

            results.add(new GameState.RaceResult(
                typists[i].getName(), positions[i], wpm,
                accPct, burnoutCounts[i], oldAcc, newAcc
            ));
        }

        state.applyRaceResults(results);

        // Show winner banner
        GameState.TypistSetup ws = state.typistSetups.get(winnerIdx);
        winnerLabel.setText("🏆  " + winnerName + " wins!");
        winnerLabel.setForeground(safeDecodeColor(ws.color, DONE));

        // Flash the winning lane
        lanePanels[winnerIdx].setBackground(new Color(20, 45, 30));

        viewResultsBtn.setVisible(true);
        updateDisplay();
    }

    private int[] computePositions()
    {
        int n = typists.length;
        int[] progress = new int[n];
        for (int i = 0; i < n; i++) progress[i] = typists[i].getProgress();

        int[] positions = new int[n];
        for (int i = 0; i < n; i++)
        {
            int rank = 1;
            for (int j = 0; j < n; j++)
            {
                if (j != i && progress[j] > progress[i]) rank++;
            }
            positions[i] = rank;
        }
        return positions;
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

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

    private Color safeDecodeColor(String hex, Color fallback)
    {
        try { return Color.decode(hex); }
        catch (Exception e) { return fallback; }
    }
}
