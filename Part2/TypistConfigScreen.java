import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Typist customisation screen. One card per typist, side-scrollable.
 * Each card allows name, symbol, typing style, keyboard, colour and accessories.
 */
public class TypistConfigScreen extends JPanel
{
    private static final Color BG     = new Color(15, 15, 25);
    private static final Color CARD   = new Color(25, 25, 40);
    private static final Color ACCENT = new Color(82, 200, 255);
    private static final Color TEXT   = new Color(220, 220, 235);
    private static final Color MUTED  = new Color(120, 120, 145);

    private static final String[] STYLES    = {"Touch Typist","Hunt & Peck","Phone Thumbs","Voice-to-Text"};
    private static final String[] KEYBOARDS = {"Mechanical","Membrane","Touchscreen","Stenography"};
    private static final String[] STYLE_DESC = {
        "+10% accuracy",
        "-10% accuracy",
        "-5% accuracy",
        "-15% accuracy"
    };
    private static final String[] KB_DESC = {
        "+5% accuracy",
        "No modifier",
        "-10% accuracy",
        "+15% accuracy"
    };
    private static final String[] COLORS = {
        "#52C8FF","#FF6B6B","#5CDB95","#FFD166","#C77DFF","#FF9F43"
    };

    private final GameState state;
    private final Runnable  onBack;
    private final Runnable  onNext;

    // Per-typist UI components
    private List<JTextField>  nameFields    = new ArrayList<>();
    private List<JTextField>  symbolFields  = new ArrayList<>();
    private List<JComboBox<String>> styleBoxes = new ArrayList<>();
    private List<JComboBox<String>> kbBoxes    = new ArrayList<>();
    private List<JCheckBox>   wristBoxes    = new ArrayList<>();
    private List<JCheckBox>   energyBoxes   = new ArrayList<>();
    private List<JCheckBox>   headphoneBoxes = new ArrayList<>();
    private List<JSlider>     accuracySliders = new ArrayList<>();
    private List<JLabel>      colorPreviews = new ArrayList<>();
    private List<Integer>     selectedColorIdx = new ArrayList<>();

    public TypistConfigScreen(GameState state, Runnable onBack, Runnable onNext)
    {
        this.state  = state;
        this.onBack = onBack;
        this.onNext = onNext;
        setBackground(BG);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI()
    {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);
        header.setBorder(BorderFactory.createEmptyBorder(24, 40, 12, 40));

        JLabel title = makeLabel("CUSTOMISE TYPISTS", 28, ACCENT, Font.BOLD);
        header.add(title, BorderLayout.WEST);

        JLabel sub = makeLabel("Configure each competitor", 13, MUTED, Font.PLAIN);
        header.add(sub, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Scrollable typist cards
        JPanel cardsRow = new JPanel();
        cardsRow.setBackground(BG);
        cardsRow.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 16));
        cardsRow.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));

        for (int i = 0; i < state.typistSetups.size(); i++)
        {
            cardsRow.add(buildTypistCard(i));
        }

        JScrollPane scroll = new JScrollPane(cardsRow);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        // Footer buttons
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 16));
        footer.setBackground(BG);

        JButton back = makeButton("← BACK", new Color(50, 50, 75), TEXT);
        back.addActionListener(e -> onBack.run());
        footer.add(back);

        JButton next = makeButton("START RACE →", new Color(82, 200, 255), new Color(10, 10, 20));
        next.addActionListener(e -> applyAndNext());
        footer.add(next);

        add(footer, BorderLayout.SOUTH);
    }

    private JPanel buildTypistCard(int idx)
    {
        GameState.TypistSetup ts = state.typistSetups.get(idx);

        JPanel card = new JPanel();
        card.setBackground(CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(260, 520));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 90), 1),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        // Typist number header
        String colorHex = ts.color;
        JLabel numLabel = makeLabel("TYPIST " + (idx + 1), 11, Color.decode(colorHex), Font.BOLD);
        numLabel.setAlignmentX(LEFT_ALIGNMENT);
        card.add(numLabel);
        card.add(Box.createVerticalStrut(12));

        // Name
        card.add(fieldLabel("Name"));
        JTextField nameField = styledField(ts.name);
        nameField.setAlignmentX(LEFT_ALIGNMENT);
        card.add(nameField);
        nameFields.add(nameField);
        card.add(Box.createVerticalStrut(10));

        // Symbol
        card.add(fieldLabel("Symbol (single character)"));
        JTextField symField = styledField(String.valueOf(ts.symbol));
        symField.setAlignmentX(LEFT_ALIGNMENT);
        card.add(symField);
        symbolFields.add(symField);
        card.add(Box.createVerticalStrut(10));

        // Typing style
        card.add(fieldLabel("Typing Style"));
        JComboBox<String> styleBox = styledCombo(STYLES);
        styleBox.setAlignmentX(LEFT_ALIGNMENT);
        // Add description label that updates on selection
        JLabel styleDesc = makeLabel(STYLE_DESC[0], 11, MUTED, Font.PLAIN);
        styleDesc.setAlignmentX(LEFT_ALIGNMENT);
        styleBox.addActionListener(e -> styleDesc.setText(STYLE_DESC[styleBox.getSelectedIndex()]));
        card.add(styleBox);
        card.add(styleDesc);
        styleBoxes.add(styleBox);
        card.add(Box.createVerticalStrut(10));

        // Keyboard
        card.add(fieldLabel("Keyboard Type"));
        JComboBox<String> kbBox = styledCombo(KEYBOARDS);
        kbBox.setAlignmentX(LEFT_ALIGNMENT);
        JLabel kbDesc = makeLabel(KB_DESC[0], 11, MUTED, Font.PLAIN);
        kbDesc.setAlignmentX(LEFT_ALIGNMENT);
        kbBox.addActionListener(e -> kbDesc.setText(KB_DESC[kbBox.getSelectedIndex()]));
        card.add(kbBox);
        card.add(kbDesc);
        kbBoxes.add(kbBox);
        card.add(Box.createVerticalStrut(10));

        // Base accuracy slider
        card.add(fieldLabel("Base Accuracy"));
        JSlider slider = new JSlider(5, 95, (int)(ts.baseAccuracy * 100));
        slider.setBackground(CARD);
        slider.setForeground(TEXT);
        slider.setAlignmentX(LEFT_ALIGNMENT);
        JLabel accVal = makeLabel(slider.getValue() + "%", 11, ACCENT, Font.BOLD);
        accVal.setAlignmentX(LEFT_ALIGNMENT);
        slider.addChangeListener(e -> accVal.setText(slider.getValue() + "%"));
        card.add(slider);
        card.add(accVal);
        accuracySliders.add(slider);
        card.add(Box.createVerticalStrut(10));

        // Accessories
        card.add(fieldLabel("Accessories"));
        JCheckBox wrist   = styledCheckbox("Wrist Support (+2%, less burnout)");
        JCheckBox energy  = styledCheckbox("Energy Drink (boost first half)");
        JCheckBox phones  = styledCheckbox("Headphones (-5% mistype chance)");
        wrist.setAlignmentX(LEFT_ALIGNMENT);
        energy.setAlignmentX(LEFT_ALIGNMENT);
        phones.setAlignmentX(LEFT_ALIGNMENT);
        card.add(wrist);
        card.add(energy);
        card.add(phones);
        wristBoxes.add(wrist);
        energyBoxes.add(energy);
        headphoneBoxes.add(phones);
        card.add(Box.createVerticalStrut(10));

        // Color picker
        card.add(fieldLabel("Lane Colour"));
        JPanel colorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        colorRow.setBackground(CARD);
        colorRow.setAlignmentX(LEFT_ALIGNMENT);
        selectedColorIdx.add(idx < COLORS.length ? idx : 0);

        for (int c = 0; c < COLORS.length; c++)
        {
            final int ci = c;
            final int typistIdx = idx;
            JButton swatch = new JButton();
            swatch.setPreferredSize(new Dimension(22, 22));
            swatch.setBackground(Color.decode(COLORS[c]));
            swatch.setBorder(BorderFactory.createLineBorder(Color.decode(COLORS[c]).darker(), 1));
            swatch.setFocusPainted(false);
            swatch.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            swatch.addActionListener(e -> {
                selectedColorIdx.set(typistIdx, ci);
                numLabel.setForeground(Color.decode(COLORS[ci]));
            });
            colorRow.add(swatch);
        }
        card.add(colorRow);

        return card;
    }

    private void applyAndNext()
    {
        for (int i = 0; i < state.typistSetups.size(); i++)
        {
            GameState.TypistSetup ts = state.typistSetups.get(i);

            String name = nameFields.get(i).getText().trim();
            if (!name.isEmpty()) ts.name = name;

            String sym = symbolFields.get(i).getText().trim();
            if (!sym.isEmpty()) ts.symbol = sym.charAt(0);

            ts.typingStyle  = (String) styleBoxes.get(i).getSelectedItem();
            ts.keyboardType = (String) kbBoxes.get(i).getSelectedItem();
            ts.baseAccuracy = accuracySliders.get(i).getValue() / 100.0;
            ts.wristSupport = wristBoxes.get(i).isSelected();
            ts.energyDrink  = energyBoxes.get(i).isSelected();
            ts.headphones   = headphoneBoxes.get(i).isSelected();

            int ci = selectedColorIdx.get(i);
            ts.color = COLORS[ci < COLORS.length ? ci : 0];
        }
        onNext.run();
    }

    // -----------------------------------------------------------------------
    // UI helpers
    // -----------------------------------------------------------------------

    private JLabel fieldLabel(String text)
    {
        JLabel l = makeLabel(text, 11, MUTED, Font.BOLD);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    private JLabel makeLabel(String text, int size, Color color, int style)
    {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Monospaced", style, size));
        l.setForeground(color);
        return l;
    }

    private JTextField styledField(String text)
    {
        JTextField tf = new JTextField(text);
        tf.setFont(new Font("Monospaced", Font.PLAIN, 13));
        tf.setBackground(new Color(35, 35, 55));
        tf.setForeground(TEXT);
        tf.setCaretColor(ACCENT);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 90)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        return tf;
    }

    private JComboBox<String> styledCombo(String[] items)
    {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(new Font("Monospaced", Font.PLAIN, 12));
        cb.setBackground(new Color(35, 35, 55));
        cb.setForeground(TEXT);
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        return cb;
    }

    private JCheckBox styledCheckbox(String text)
    {
        JCheckBox cb = new JCheckBox(text);
        cb.setFont(new Font("Monospaced", Font.PLAIN, 11));
        cb.setForeground(TEXT);
        cb.setBackground(CARD);
        cb.setFocusPainted(false);
        return cb;
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
