package listfix.view.dialogs;

import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;
import com.jgoodies.looks.plastic.theme.DarkStar;
import com.jgoodies.looks.plastic.theme.SkyBlue;
import com.jgoodies.looks.windows.WindowsLookAndFeel;
import listfix.json.JsonAppOptions;
import listfix.util.OperatingSystem;
import listfix.view.support.FontExtensions;
import say.swing.JFontChooser;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class AppOptionsDialog extends JDialog
{

  public static final int OK = 0;
  public static final int CANCEL = 1;
  private static final long serialVersionUID = 3409894354485158935L;
  public static final String TEXT_FIELD_TRAILING_SPACER = "     ";
  private final FolderChooser _jMediaDirChooser = new FolderChooser();
  private int _resultCode;
  private String _fileName;
  private JsonAppOptions _options = null;
  private Font _chosenFont = null;
  private LookAndFeelInfo[] _installedLookAndFeelInfos = null;

  private void ApplyOperatingSystemBasedVisibility()
  {
    if (!OperatingSystem.isWindows())
    {
      _pnlUseUnc.setVisible(false);
    }
    else if (OperatingSystem.isWindows())
    {
      _pnlDisableCaseSensitivity.setVisible(false);
    }
  }

  private static class IntegerRangeComboBoxModel extends AbstractListModel implements ComboBoxModel
  {
    private final List<Integer> intList = new ArrayList<>();
    Object _selected;

    IntegerRangeComboBoxModel(int i, int i0)
    {
      for (int j = i; j <= i0; j++)
      {
        intList.add(j);
      }
    }

    @Override
    public void setSelectedItem(Object anItem)
    {
      _selected = anItem;
    }

    @Override
    public Object getSelectedItem()
    {
      return _selected;
    }

    @Override
    public int getSize()
    {
      return intList.size();
    }

    @Override
    public Object getElementAt(int index)
    {
      return intList.get(index);
    }
  }

  /**
   * Creates new form EditFilenameDialog
   */
  public AppOptionsDialog(Frame parent, String title, boolean modal, JsonAppOptions opts)
  {
    super(parent, title, modal);
    if (opts == null)
    {
      _options = new JsonAppOptions();
    }
    else
    {
      _options = opts;
    }
    initComponents();
    _chosenFont = _options.getAppFont();
    _fontDisplayLabel.setText(FontExtensions.formatFont(_chosenFont));
    ApplyOperatingSystemBasedVisibility();
    initPlaylistDirectoryFolderChooser();
  }

  private void initPlaylistDirectoryFolderChooser()
  {
    _jMediaDirChooser.setDialogTitle("Specify a playlists directory...");
    _jMediaDirChooser.setAcceptAllFileFilterUsed(false);
    _jMediaDirChooser.setMinimumSize(new Dimension(400, 500));
    _jMediaDirChooser.setPreferredSize(new Dimension(400, 500));
  }

  public AppOptionsDialog()
  {
  }

  public String getFileName()
  {
    return _fileName;
  }

  public void setFileName(String x)
  {
    _fileName = x;
  }

  public void setResultCode(int i)
  {
    _resultCode = i;
  }

  public int getResultCode()
  {
    return _resultCode;
  }

  private LookAndFeelInfo[] getInstalledLookAndFeels()
  {
    if (_installedLookAndFeelInfos == null)
    {
      LookAndFeelInfo[] plafs = UIManager.getInstalledLookAndFeels();
      List<LookAndFeelInfo> lafs = new ArrayList<>();
      for (LookAndFeelInfo laf : plafs)
      {
        if (!laf.getName().toLowerCase().contains("nimbus"))
        {
          lafs.add(laf);
        }
      }

      // "Install" the L&Fs we have internally added.
      if (OperatingSystem.isWindows())
      {
        lafs.add(new LookAndFeelInfo("Windows (Plastic)", WindowsLookAndFeel.class.getName()));
      }
      lafs.add(new LookAndFeelInfo("Plastic - Default", PlasticLookAndFeel.class.getName()));
      lafs.add(new LookAndFeelInfo("Plastic - DarkStar", DarkStar.class.getName()));
      lafs.add(new LookAndFeelInfo("Plastic - SkyBlue", SkyBlue.class.getName()));
      lafs.add(new LookAndFeelInfo("Plastic3D", Plastic3DLookAndFeel.class.getName()));
      lafs.add(new LookAndFeelInfo("PlasticXP", PlasticXPLookAndFeel.class.getName()));

      _installedLookAndFeelInfos = lafs.toArray(new LookAndFeelInfo[0]);
    }
    return _installedLookAndFeelInfos;
  }

  private DefaultComboBoxModel getLookAndFeelMenuItems()
  {
    LookAndFeelInfo[] plafs = getInstalledLookAndFeels();

    List<String> model = new ArrayList<>();
    for (LookAndFeelInfo plaf : plafs)
    {
      model.add(plaf.getName());
    }
    return new DefaultComboBoxModel(model.toArray(new String[0]));
  }

  private LookAndFeelInfo getInstalledLookAndFeelAtIndex(int index)
  {
    UIManager.LookAndFeelInfo[] plafs = getInstalledLookAndFeels();
    if (index < plafs.length)
    {
      return plafs[index];
    }
    return plafs[0];
  }

  private LookAndFeelInfo getInstalledLookAndFeelByClassName(String name)
  {
    UIManager.LookAndFeelInfo[] plafs = getInstalledLookAndFeels();
    for (LookAndFeelInfo plaf : plafs)
    {
      if (name.equals(plaf.getClassName()))
      {
        return plaf;
      }
    }
    return plafs[0];
  }


  public JsonAppOptions showDialog()
  {
    this.center();
    this.setVisible(true);
    if (this.getResultCode() == OK)
    {
      _options.setAutoLocateEntriesOnPlaylistLoad(autoLocateCheckBox.isSelected());
      _options.setMaxPlaylistHistoryEntries(((Integer) recentPlaylistLimitComboBox.getItemAt(recentPlaylistLimitComboBox.getSelectedIndex())));
      _options.setSavePlaylistsWithRelativePaths(relativePathsCheckBox.isSelected());
      _options.setAutoRefreshMediaLibraryOnStartup(autoRefreshOnStartupCheckBox.isSelected());
      _options.setLookAndFeel(getInstalledLookAndFeelAtIndex(lookAndFeelComboBox.getSelectedIndex()).getClassName());
      _options.setAlwaysUseUNCPaths(alwaysUseUNCPathsCheckBox.isSelected());
      _options.setAppFont(_chosenFont);
      _options.setMaxClosestResults(((Integer) _cbxMaxClosestMatches.getItemAt(_cbxMaxClosestMatches.getSelectedIndex())));
      _options.setIgnoredSmallWords(_smallWordsTxtField.getText().trim());
      _options.setCaseInsensitiveExactMatching(_cbxCaseSensitivity.isSelected());
    }
    return _options;
  }

  private void center()
  {
    Point parentLocation = this.getParent().getLocationOnScreen();
    double x = parentLocation.getX();
    double y = parentLocation.getY();
    int width = this.getParent().getWidth();
    int height = this.getParent().getHeight();

    this.setLocation((int) x + (width - this.getPreferredSize().width) / 2, (int) y + (height - this.getPreferredSize().height) / 2);
  }

  /**
   * This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents()
  {
    GridBagConstraints gridBagConstraints;

    topPanel = new JPanel();
    optionsPanel = new JPanel();
    _pnlLookAndFeel = new JPanel();
    jLabel5 = new JLabel();
    lookAndFeelComboBox = new JComboBox();
    _pnlfontChooser = new JPanel();
    jLabel8 = new JLabel();
    _fontDisplayLabel = new JLabel();
    _changeFontButton = new JButton();
    _pnlNumClosestMatches = new JPanel();
    jLabel9 = new JLabel();
    _cbxMaxClosestMatches = new JComboBox();
    _pnlRecentListLimit = new JPanel();
    jLabel1 = new JLabel();
    recentPlaylistLimitComboBox = new JComboBox();
    _pnlUseUnc = new JPanel();
    jLabel6 = new JLabel();
    alwaysUseUNCPathsCheckBox = new JCheckBox();
    _pnlSaveRelative = new JPanel();
    jLabel3 = new JLabel();
    relativePathsCheckBox = new JCheckBox();
    _pnlAutoLocate = new JPanel();
    jLabel2 = new JLabel();
    autoLocateCheckBox = new JCheckBox();
    _pnRefreshMediaLibraryOnStart = new JPanel();
    jLabel4 = new JLabel();
    autoRefreshOnStartupCheckBox = new JCheckBox();
    _pnlDisableCaseSensitivity = new JPanel();
    _lblCaseSensitivity = new JLabel();
    _cbxCaseSensitivity = new JCheckBox();

    _pnlSmallWords = new JPanel();
    jLabel10 = new JLabel();
    _smallWordsTxtField = new JTextField();
    buttonPanel = new JPanel();
    jButton1 = new JButton();
    jButton2 = new JButton();

    setMinimumSize(new Dimension(480, 385));
    setModal(true);
    addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowClosing(WindowEvent evt)
      {
        closeDialog();
      }
    });
    getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

    topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

    optionsPanel.setBorder(BorderFactory.createTitledBorder("General Config"));
    optionsPanel.setLayout(new GridBagLayout());

    _pnlLookAndFeel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));

    jLabel5.setText("Look and Feel:");
    jLabel5.setVerticalAlignment(SwingConstants.TOP);
    jLabel5.setMinimumSize(new Dimension(111, 9));
    jLabel5.setVerticalTextPosition(SwingConstants.TOP);
    _pnlLookAndFeel.add(jLabel5);

    lookAndFeelComboBox.setModel(this.getLookAndFeelMenuItems());
    lookAndFeelComboBox.setSelectedItem(this.getInstalledLookAndFeelByClassName(_options.getLookAndFeel()).getName());
    _pnlLookAndFeel.add(lookAndFeelComboBox);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.anchor = GridBagConstraints.LINE_START;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.insets = new Insets(3, 0, 3, 0);
    optionsPanel.add(_pnlLookAndFeel, gridBagConstraints);

    _pnlfontChooser.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));

    jLabel8.setText("Font:");
    _pnlfontChooser.add(jLabel8);

    _fontDisplayLabel.setText("SansSerif, Plain, 10");
    _pnlfontChooser.add(_fontDisplayLabel);

    _changeFontButton.setText("...");
    _changeFontButton.setToolTipText("Choose Font");
    _changeFontButton.setAlignmentY(0.0F);
    _changeFontButton.setMargin(new Insets(2, 3, 2, 3));
    _changeFontButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent evt)
      {
        _changeFontButtonActionPerformed();
      }
    });
    _pnlfontChooser.add(_changeFontButton);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.anchor = GridBagConstraints.LINE_START;
    optionsPanel.add(_pnlfontChooser, gridBagConstraints);

    _pnlNumClosestMatches.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));

    jLabel9.setText("Number of closest matches to return when searching:");
    jLabel9.setToolTipText("This value has memory usage implications, if you run out of memory while repairing a list, dial this down to 20 or less.");
    jLabel9.setMinimumSize(new Dimension(257, 20));
    _pnlNumClosestMatches.add(jLabel9);

    _cbxMaxClosestMatches.setModel(new IntegerRangeComboBoxModel(10, 100));
    _cbxMaxClosestMatches.setSelectedItem(_options.getMaxClosestResults());
    _pnlNumClosestMatches.add(_cbxMaxClosestMatches);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
    gridBagConstraints.anchor = GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new Insets(3, 0, 3, 0);
    optionsPanel.add(_pnlNumClosestMatches, gridBagConstraints);

    _pnlRecentListLimit.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));

    jLabel1.setText("Recent Playlist Limit: ");
    jLabel1.setVerticalAlignment(SwingConstants.TOP);
    jLabel1.setMinimumSize(new Dimension(111, 20));
    jLabel1.setVerticalTextPosition(SwingConstants.TOP);
    _pnlRecentListLimit.add(jLabel1);

    recentPlaylistLimitComboBox.setModel(new IntegerRangeComboBoxModel(1, 15));
    recentPlaylistLimitComboBox.setSelectedItem(_options.getMaxPlaylistHistoryEntries());
    recentPlaylistLimitComboBox.setMaximumSize(null);
    recentPlaylistLimitComboBox.setMinimumSize(null);
    recentPlaylistLimitComboBox.setPreferredSize(null);
    _pnlRecentListLimit.add(recentPlaylistLimitComboBox);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.anchor = GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new Insets(3, 0, 3, 0);
    optionsPanel.add(_pnlRecentListLimit, gridBagConstraints);

    _pnlUseUnc.setMinimumSize(new Dimension(165, 20));
    _pnlUseUnc.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));

    jLabel6.setText("Media library uses UNC paths for directories on mapped drives:");
    jLabel6.setVerticalAlignment(SwingConstants.TOP);
    jLabel6.setMinimumSize(new Dimension(111, 20));
    jLabel6.setVerticalTextPosition(SwingConstants.TOP);
    _pnlUseUnc.add(jLabel6);

    alwaysUseUNCPathsCheckBox.setSelected(_options.getAlwaysUseUNCPaths());
    _pnlUseUnc.add(alwaysUseUNCPathsCheckBox);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.anchor = GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new Insets(3, 0, 3, 0);
    optionsPanel.add(_pnlUseUnc, gridBagConstraints);

    _pnlSaveRelative.setMinimumSize(new Dimension(165, 20));
    _pnlSaveRelative.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));

    jLabel3.setText("Save playlists with relative file references:");
    jLabel3.setVerticalAlignment(SwingConstants.TOP);
    jLabel3.setMinimumSize(new Dimension(111, 20));
    jLabel3.setVerticalTextPosition(SwingConstants.TOP);
    _pnlSaveRelative.add(jLabel3);

    relativePathsCheckBox.setSelected(_options.getSavePlaylistsWithRelativePaths());
    _pnlSaveRelative.add(relativePathsCheckBox);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 5;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.anchor = GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new Insets(3, 0, 3, 0);
    optionsPanel.add(_pnlSaveRelative, gridBagConstraints);

    _pnlAutoLocate.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));

    jLabel2.setText("Auto-locate missing playlist entries on load:");
    jLabel2.setVerticalAlignment(SwingConstants.TOP);
    jLabel2.setMinimumSize(new Dimension(111, 20));
    jLabel2.setVerticalTextPosition(SwingConstants.TOP);
    _pnlAutoLocate.add(jLabel2);

    autoLocateCheckBox.setSelected(_options.getAutoLocateEntriesOnPlaylistLoad());
    _pnlAutoLocate.add(autoLocateCheckBox);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 6;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.anchor = GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new Insets(3, 0, 3, 0);
    optionsPanel.add(_pnlAutoLocate, gridBagConstraints);

    _pnRefreshMediaLibraryOnStart.setMinimumSize(new Dimension(165, 20));
    _pnRefreshMediaLibraryOnStart.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));

    jLabel4.setText("Auto refresh media library at startup:");
    jLabel4.setVerticalAlignment(SwingConstants.TOP);
    jLabel4.setMinimumSize(new Dimension(111, 20));
    jLabel4.setVerticalTextPosition(SwingConstants.TOP);
    _pnRefreshMediaLibraryOnStart.add(jLabel4);

    autoRefreshOnStartupCheckBox.setSelected(_options.getAutoRefreshMediaLibraryOnStartup());
    _pnRefreshMediaLibraryOnStart.add(autoRefreshOnStartupCheckBox);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 7;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.anchor = GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new Insets(3, 0, 3, 0);
    optionsPanel.add(_pnRefreshMediaLibraryOnStart, gridBagConstraints);

    _pnlDisableCaseSensitivity.setMinimumSize(new Dimension(165, 20));
    _pnlDisableCaseSensitivity.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));

    _lblCaseSensitivity.setText("Exact matches ignore upper/lower case:");
    _lblCaseSensitivity.setVerticalAlignment(SwingConstants.TOP);
    _lblCaseSensitivity.setMinimumSize(new Dimension(111, 20));
    _lblCaseSensitivity.setVerticalTextPosition(SwingConstants.TOP);
    _pnlDisableCaseSensitivity.add(_lblCaseSensitivity);

    _cbxCaseSensitivity.setSelected(_options.getCaseInsensitiveExactMatching());
    _pnlDisableCaseSensitivity.add(_cbxCaseSensitivity);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 8;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.anchor = GridBagConstraints.LINE_START;
    gridBagConstraints.insets = new Insets(3, 0, 3, 0);
    optionsPanel.add(_pnlDisableCaseSensitivity, gridBagConstraints);

    _pnlSmallWords.setMinimumSize(new Dimension(165, 20));
    _pnlSmallWords.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));

    jLabel10.setText("Words to ignore while finding closest matches:");
    jLabel10.setVerticalAlignment(SwingConstants.TOP);
    jLabel10.setMinimumSize(new Dimension(111, 9));
    jLabel10.setVerticalTextPosition(SwingConstants.TOP);
    _pnlSmallWords.add(jLabel10);

    _smallWordsTxtField.setHorizontalAlignment(JTextField.LEFT);
    _smallWordsTxtField.setText(_options.getIgnoredSmallWords() + TEXT_FIELD_TRAILING_SPACER);
    _smallWordsTxtField.setMinimumSize(new Dimension(150, 20));
    _smallWordsTxtField.setPreferredSize(null);
    _pnlSmallWords.add(_smallWordsTxtField);

    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 10;
    gridBagConstraints.fill = GridBagConstraints.BOTH;
    gridBagConstraints.anchor = GridBagConstraints.LINE_START;
    gridBagConstraints.weighty = 1.0;
    gridBagConstraints.insets = new Insets(3, 0, 3, 0);
    optionsPanel.add(_pnlSmallWords, gridBagConstraints);

    topPanel.add(optionsPanel);

    buttonPanel.setMaximumSize(null);
    buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

    jButton1.setText("OK");
    jButton1.setMinimumSize(new Dimension(49, 20));
    jButton1.setPreferredSize(null);
    jButton1.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent evt)
      {
        jButton1ActionPerformed();
      }
    });
    buttonPanel.add(jButton1);

    jButton2.setText("Cancel");
    jButton2.setMinimumSize(new Dimension(67, 20));
    jButton2.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent evt)
      {
        jButton2ActionPerformed();
      }
    });
    buttonPanel.add(jButton2);

    topPanel.add(buttonPanel);

    getContentPane().add(topPanel);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void jButton2ActionPerformed()
  {//GEN-FIRST:event_jButton2ActionPerformed
    setVisible(false);
    dispose();
    setResultCode(CANCEL);
  }//GEN-LAST:event_jButton2ActionPerformed

  private void jButton1ActionPerformed()
  {//GEN-FIRST:event_jButton1ActionPerformed
    setVisible(false);
    dispose();
    setResultCode(OK);
  }//GEN-LAST:event_jButton1ActionPerformed

  /**
   * Closes the dialog
   */
  private void closeDialog()
  {//GEN-FIRST:event_closeDialog
    setVisible(false);
    dispose();
  }//GEN-LAST:event_closeDialog

  private void _changeFontButtonActionPerformed()//GEN-FIRST:event__changeFontButtonActionPerformed
  {//GEN-HEADEREND:event__changeFontButtonActionPerformed
    JFontChooser jfc = new JFontChooser();
    jfc.setSelectedFont(_chosenFont);
    jfc.showDialog(this);
    _chosenFont = jfc.getSelectedFont();
    _fontDisplayLabel.setText(FontExtensions.formatFont(_chosenFont));
  }//GEN-LAST:event__changeFontButtonActionPerformed

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private JCheckBox _cbxCaseSensitivity;
  private JComboBox _cbxMaxClosestMatches;
  private JButton _changeFontButton;
  private JLabel _fontDisplayLabel;
  private JLabel _lblCaseSensitivity;
  private JPanel _pnRefreshMediaLibraryOnStart;
  private JPanel _pnlAutoLocate;
  private JPanel _pnlDisableCaseSensitivity;

  private JPanel _pnlLookAndFeel;
  private JPanel _pnlNumClosestMatches;
  private JPanel _pnlRecentListLimit;
  private JPanel _pnlSaveRelative;
  private JPanel _pnlSmallWords;
  private JPanel _pnlUseUnc;
  private JPanel _pnlfontChooser;
  private JTextField _smallWordsTxtField;
  private JCheckBox alwaysUseUNCPathsCheckBox;
  private JCheckBox autoLocateCheckBox;
  private JCheckBox autoRefreshOnStartupCheckBox;
  private JPanel buttonPanel;
  private JButton jButton1;
  private JButton jButton2;
  private JLabel jLabel1;
  private JLabel jLabel10;
  private JLabel jLabel2;
  private JLabel jLabel3;
  private JLabel jLabel4;
  private JLabel jLabel5;
  private JLabel jLabel6;
  private JLabel jLabel8;
  private JLabel jLabel9;
  private JComboBox lookAndFeelComboBox;
  private JPanel optionsPanel;

  private JComboBox recentPlaylistLimitComboBox;
  private JCheckBox relativePathsCheckBox;
  private JPanel topPanel;
  // End of variables declaration//GEN-END:variables
}
