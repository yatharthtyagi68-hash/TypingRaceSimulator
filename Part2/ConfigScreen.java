import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Configuration screen: choose passage, number of typists, and difficulty modifiers.
 */
public class ConfigScreen extends JPanel
{
    private static final Color BG        = new Color(15, 15, 25);
    private static final Color CARD      = new Color(25, 25, 40);
    private static final Color ACCENT    = new Color(82, 200, 255);
    private static final Color TEXT      = new Color(220, 220, 235);
    private static final Color MUTED     = new Color(120, 120, 145);
    private static final Color BTN_BG    = new Color(82, 200, 255);
    private static final Color BTN_TEXT  = new Color(10, 10, 20);

    private static final String[] PASSAGES = {
        "The quick brown fox jumps over the lazy dog.",
        "To be or not to be, that is the question worth asking every single day.",
        "All that glitters is not gold; often have you heard that told in many tales.",
        "In the beginning was the word, and the word was with the universe entire.",
        "It was a bright cold day in April and the clocks were striking thirteen loud."
    };
    private static final String[] PASSAGE_LABELS = {
        "Short  — 44 chars",
        "Medium — 71 chars",
        "Medium — 75 chars",
        "Long   — 79 chars",
        "Long   — 80 chars"
    };

    private JComboBox<String> passageCombo;
    private JTextField        customPassage;
    private JSpinner          seatCountSpinner;
    private JCheckBox         autocorrectBox;
    private JCheckBox         caffeineModeBox;
    private JCheckBox         nightShiftBox;

    private final GameState   state;
    private final Runnable    onNext;

    public ConfigScreen(GameState state, Runnable onNext)
    {
        this.state  = state;
        this.onNext = onNext;
        setBackground(BG);
        setLayout(new GridBagLayout());
        buildUI();
    }

    private void buildUI()
    {
        JPanel centre = new JPanel();
        centre.setBackground(BG);
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));
        centre.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));

        // Title
        JLabel title = makeLabel("TYPING RACE", 36, ACCENT, Font.BOLD);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        centre.add(title);

        JLabel sub = makeLabel("Configure your race", 14, MUTED, Font.PLAIN);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        centre.add(sub);
        centre.add(Box.createVerticalStrut(32));

        // Passage card
        centre.add(makeCard("PASSAGE SELECTION", buildPassagePanel()));
        centre.add(Box.createVerticalStrut(16));

        // Race setup card
        centre.add(makeCard("RACE SETUP", buildRaceSetupPanel()));
        centre.add(Box.createVerticalStrut(16));

        // Difficulty card
        centre.add(makeCard("DIFFICULTY MODIFIERS", buildDifficultyPanel()));
        centre.add(Box.createVerticalStrut(32));

        // Next button
        JButton next = makeButton("CONFIGURE TYPISTS →");
        next.setAlignmentX(Component.CENTER_ALIGNMENT);
        next.addActionListener(e -> applyAndNext());
        centre.add(next);

        add(centre);
    }

    private JPanel buildPassagePanel()
    {
        JPanel p = transparent();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        passageCombo = new JComboBox<>(PASSAGE_LABELS);
        styleCombo(passageCombo);
        passageCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(passageCombo);
        p.add(Box.createVerticalStrut(10));

        JLabel customLabel = makeLabel("Or enter a custom passage:", 12, MUTED, Font.PLAIN);
        customLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(customLabel);
        p.add(Box.createVerticalStrut(6));

        customPassage = new JTextField();
        styleTextField(customPassage);
        customPassage.setAlignmentX(Component.LEFT_ALIGNMENT);
        customPassage.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        p.add(customPassage);

        return p;
    }

    private JPanel buildRaceSetupPanel()
    {
        JPanel p = transparent();
        p.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

        JLabel label = makeLabel("Number of typists:  ", 14, TEXT, Font.PLAIN);
        p.add(label);

        SpinnerNumberModel model = new SpinnerNumberModel(3, 2, 6, 1);
        seatCountSpinner = new JSpinner(model);
        styleSpinner(seatCountSpinner);
        p.add(seatCountSpinner);

        return p;
    }

    private JPanel buildDifficultyPanel()
    {
        JPanel p = transparent();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        autocorrectBox  = makeCheckbox("Autocorrect ON — slideBack amount halved");
        caffeineModeBox = makeCheckbox("Caffeine Mode — speed boost for first 10 turns, then higher burnout risk");
        nightShiftBox   = makeCheckbox("Night Shift — all accuracy ratings slightly reduced");

        p.add(autocorrectBox);
        p.add(Box.createVerticalStrut(8));
        p.add(caffeineModeBox);
        p.add(Box.createVerticalStrut(8));
        p.add(nightShiftBox);

        return p;
    }

    private void applyAndNext()
    {
        // Determine passage
        String custom = customPassage.getText().trim();
        if (!custom.isEmpty())
        {
            state.passageText   = custom;
            state.passageLength = custom.length();
        }
        else
        {
            int idx = passageCombo.getSelectedIndex();
            state.passageText   = PASSAGES[idx];
            state.passageLength = PASSAGES[idx].length();
        }

        // Seat count — initialise typist setups
        int seats = (Integer) seatCountSpinner.getValue();
        state.typistSetups.clear();
        char[] symbols = {'①','②','③','④','⑤','⑥'};
        String[] defaultNames = {"TURBOFINGERS","QWERTY_QUEEN","HUNT_N_PECK",
                                  "SWIFT_KEYS","TYPINATOR","KEYMASTER"};
        String[] defaultColors = {"#52C8FF","#FF6B6B","#5CDB95","#FFD166","#C77DFF","#FF9F43"};
        for (int i = 0; i < seats; i++)
        {
            GameState.TypistSetup ts = new GameState.TypistSetup(defaultNames[i], symbols[i]);
            ts.color = defaultColors[i];
            state.typistSetups.add(ts);
        }

        // Difficulty modifiers
        state.autocorrectOn = autocorrectBox.isSelected();
        state.caffeineMode  = caffeineModeBox.isSelected();
        state.nightShift    = nightShiftBox.isSelected();

        onNext.run();
    }

    // -----------------------------------------------------------------------
    // UI helpers
    // -----------------------------------------------------------------------

    private JPanel makeCard(String title, JPanel content)
    {
        JPanel card = new JPanel();
        card.setBackground(CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 90), 1),
            BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lbl = makeLabel(title, 11, ACCENT, Font.BOLD);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        card.add(lbl);
        content.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(content);
        return card;
    }

    private JLabel makeLabel(String text, int size, Color color, int style)
    {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Monospaced", style, size));
        l.setForeground(color);
        return l;
    }

    private JButton makeButton(String text)
    {
        JButton b = new JButton(text);
        b.setFont(new Font("Monospaced", Font.BOLD, 14));
        b.setBackground(BTN_BG);
        b.setForeground(BTN_TEXT);
        b.setBorder(BorderFactory.createEmptyBorder(12, 32, 12, 32));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        return b;
    }

    private JCheckBox makeCheckbox(String text)
    {
        JCheckBox cb = new JCheckBox(text);
        cb.setFont(new Font("Monospaced", Font.PLAIN, 13));
        cb.setForeground(TEXT);
        cb.setBackground(CARD);
        cb.setFocusPainted(false);
        return cb;
    }

    private void styleCombo(JComboBox<?> combo)
    {
        combo.setFont(new Font("Monospaced", Font.PLAIN, 13));
        combo.setBackground(new Color(35, 35, 55));
        combo.setForeground(TEXT);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
    }

    private void styleTextField(JTextField tf)
    {
        tf.setFont(new Font("Monospaced", Font.PLAIN, 13));
        tf.setBackground(new Color(35, 35, 55));
        tf.setForeground(TEXT);
        tf.setCaretColor(ACCENT);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 90)),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
    }

    private void styleSpinner(JSpinner spinner)
    {
        spinner.setFont(new Font("Monospaced", Font.PLAIN, 14));
        spinner.setBackground(new Color(35, 35, 55));
        spinner.setForeground(TEXT);
        spinner.setPreferredSize(new Dimension(70, 32));
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setBackground(new Color(35, 35, 55));
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setForeground(TEXT);
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setFont(new Font("Monospaced", Font.PLAIN, 14));
    }

    private JPanel transparent()
    {
        JPanel p = new JPanel();
        p.setBackground(CARD);
        return p;
    }
}
