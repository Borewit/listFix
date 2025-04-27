package listfix.view.dialogs;

import io.github.borewit.lizzy.playlist.PlaylistFormat;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import javax.swing.*;
import listfix.io.Constants;

public class SaveAllDialog extends JDialog {

    public static final int OK = 0;
    public static final int CANCEL = 1;

    private int resultCode;
    private JComboBox<PlaylistFormat> formatComboBox;
    private JComboBox<String> encodingComboBox; // Store Charset names as Strings

    private PlaylistFormat selectedFormat;
    private Charset selectedEncoding;

    public SaveAllDialog(Frame parent) {
        super(parent, "Save All Options", true);
        initComponents();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        // Components
        formatComboBox = new JComboBox<>(PlaylistFormat.values());
        encodingComboBox = new JComboBox<>(new String[] {
                StandardCharsets.UTF_8.name(),
                Charset.defaultCharset().name() // System Default (ANSI on Windows)
        });
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        // Labels
        JLabel formatLabel = new JLabel("Output Format:");
        JLabel encodingLabel = new JLabel("Encoding:");

        // Layout
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Format Row
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(formatLabel, gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(formatComboBox, gbc);

        // Encoding Row
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(encodingLabel, gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        add(encodingComboBox, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        add(buttonPanel, gbc);

        // --- Listeners ---
        formatComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateEncodingOptions();
            }
        });

        okButton.addActionListener(e -> onOK());
        cancelButton.addActionListener(e -> onCancel());

        // Initial state
        updateEncodingOptions();
        pack();
        setResizable(false);
    }

    private void updateEncodingOptions() {
        PlaylistFormat selected = (PlaylistFormat) formatComboBox.getSelectedItem();
        if (selected == PlaylistFormat.m3u8 || selected == PlaylistFormat.xspf || selected == PlaylistFormat.wpl) {
            // These formats inherently support/require UTF-8
            encodingComboBox.setSelectedItem(StandardCharsets.UTF_8.name());
            encodingComboBox.setEnabled(false);
        } else if (selected == PlaylistFormat.pls) {
            // PLS *can* be ANSI, but UTF-8 is safer for compatibility
            encodingComboBox.setSelectedItem(StandardCharsets.UTF_8.name());
            encodingComboBox.setEnabled(true); // Allow user choice, default UTF-8
        } else if (selected == PlaylistFormat.m3u) {
            // M3U can be either, default to UTF-8 for better compatibility
            encodingComboBox.setSelectedItem(StandardCharsets.UTF_8.name());
            encodingComboBox.setEnabled(true);
        } else {
            // Default for other/unknown formats
            encodingComboBox.setSelectedItem(StandardCharsets.UTF_8.name());
            encodingComboBox.setEnabled(true);
        }
    }

    private void onOK() {
        selectedFormat = (PlaylistFormat) formatComboBox.getSelectedItem();
        String selectedEncodingName = (String) encodingComboBox.getSelectedItem();
        try {
            selectedEncoding = Charset.forName(selectedEncodingName);
        } catch (Exception e) {
            // Fallback or handle error - should not happen with standard names
            selectedEncoding = StandardCharsets.UTF_8;
        }
        resultCode = OK;
        setVisible(false);
        dispose();
    }

    private void onCancel() {
        resultCode = CANCEL;
        setVisible(false);
        dispose();
    }

    // --- Getters ---
    public int getResultCode() {
        return resultCode;
    }

    public PlaylistFormat getSelectedFormat() {
        return selectedFormat;
    }

    public Charset getSelectedEncoding() {
        return selectedEncoding;
    }

    public static void main(String[] args) {
        // Simple test
        SwingUtilities.invokeLater(() -> {
            SaveAllDialog dialog = new SaveAllDialog(null);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
            if (dialog.getResultCode() == OK) {
                System.out.println("Format: " + dialog.getSelectedFormat());
                System.out.println("Encoding: " + dialog.getSelectedEncoding());
            } else {
                System.out.println("Cancelled");
            }
        });
    }
}