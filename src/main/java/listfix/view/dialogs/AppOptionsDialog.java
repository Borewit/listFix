/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2014 Jeremy Caron
 *
 * This file is part of listFix().
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, please see http://www.gnu.org/licenses/
 */

package listfix.view.dialogs;

/**
 *
 * @author  jcaron
 */
import com.jidesoft.swing.FolderChooser;
import java.awt.Dimension;

import java.awt.Font;
import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import listfix.json.JsonAppOptions;
import listfix.util.ExStack;
import listfix.util.OperatingSystem;
import listfix.view.controls.JTransparentTextArea;
import listfix.view.support.FontExtensions;

import net.mariottini.swing.JFontChooser;

import org.apache.log4j.Logger;

/**
 *
 * @author jcaron
 */
public class AppOptionsDialog extends javax.swing.JDialog
{
  /**
   *
   */
  public static final int OK = 0;
  /**
   *
   */
  public static final int CANCEL = 1;

  private static final long serialVersionUID = 3409894354485158935L;
  private static final Logger _logger = Logger.getLogger(AppOptionsDialog.class);
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

  /** Creates new form EditFilenameDialog
   * @param parent
   * @param title
   * @param modal
   * @param opts
   */
  public AppOptionsDialog(java.awt.Frame parent, String title, boolean modal, JsonAppOptions opts)
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
    _jMediaDirChooser.setAvailableButtons(FolderChooser.BUTTON_DESKTOP | FolderChooser.BUTTON_MY_DOCUMENTS | FolderChooser.BUTTON_NEW | FolderChooser.BUTTON_REFRESH);
    _jMediaDirChooser.setRecentListVisible(false);
    _jMediaDirChooser.setMinimumSize(new Dimension(400, 500));
    _jMediaDirChooser.setPreferredSize(new Dimension(400, 500));
  }

  /**
   *
   */
  public AppOptionsDialog()
  {
  }

  /**
   *
   * @return
   */
  public String getFileName()
  {
    return _fileName;
  }

  /**
   *
   * @param x
   */
  public void setFileName(String x)
  {
    _fileName = x;
  }

  /**
   *
   * @param i
   */
  public void setResultCode(int i)
  {
    _resultCode = i;
  }

  /**
   *
   * @return
   */
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
        lafs.add(new LookAndFeelInfo("Windows (Plastic)", com.jgoodies.looks.windows.WindowsLookAndFeel.class.getName()));
      }
      lafs.add(new LookAndFeelInfo("Plastic - Default", com.jgoodies.looks.plastic.PlasticLookAndFeel.class.getName()));
      lafs.add(new LookAndFeelInfo("Plastic - DarkStar", com.jgoodies.looks.plastic.theme.DarkStar.class.getName()));
      lafs.add(new LookAndFeelInfo("Plastic - SkyBlue", com.jgoodies.looks.plastic.theme.SkyBlue.class.getName()));
      lafs.add(new LookAndFeelInfo("Plastic3D", com.jgoodies.looks.plastic.Plastic3DLookAndFeel.class.getName()));
      lafs.add(new LookAndFeelInfo("PlasticXP", com.jgoodies.looks.plastic.PlasticXPLookAndFeel.class.getName()));

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

  /**
   *
   * @return
   */
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
      _options.setPlaylistsDirectory(playlistDirectoryTextField.getText().trim());
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

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {
        java.awt.GridBagConstraints gridBagConstraints;

        topPanel = new javax.swing.JPanel();
        optionsPanel = new javax.swing.JPanel();
        _pnlLookAndFeel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        lookAndFeelComboBox = new javax.swing.JComboBox();
        _pnlfontChooser = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        _fontDisplayLabel = new javax.swing.JLabel();
        _changeFontButton = new javax.swing.JButton();
        _pnlNumClosestMatches = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        _cbxMaxClosestMatches = new javax.swing.JComboBox();
        _pnlRecentListLimit = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        recentPlaylistLimitComboBox = new javax.swing.JComboBox();
        _pnlUseUnc = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        alwaysUseUNCPathsCheckBox = new javax.swing.JCheckBox();
        _pnlSaveRelative = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        relativePathsCheckBox = new javax.swing.JCheckBox();
        _pnlAutoLocate = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        autoLocateCheckBox = new javax.swing.JCheckBox();
        _pnRefreshMediaLibraryOnStart = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        autoRefreshOnStartupCheckBox = new javax.swing.JCheckBox();
        _pnlDisableCaseSensitivity = new javax.swing.JPanel();
        _lblCaseSensitivity = new javax.swing.JLabel();
        _cbxCaseSensitivity = new javax.swing.JCheckBox();
        _pnlListsDir = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        playlistDirectoryTextField = new javax.swing.JTextField();
        playlistDirectoryBrowseButton = new javax.swing.JButton();
        _pnlSmallWords = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        _smallWordsTxtField = new javax.swing.JTextField();
        buttonPanel = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(480, 385));
        setModal(true);
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                closeDialog(evt);
            }
        });
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        topPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topPanel.setLayout(new javax.swing.BoxLayout(topPanel, javax.swing.BoxLayout.Y_AXIS));

        optionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("General Config"));
        optionsPanel.setLayout(new java.awt.GridBagLayout());

        _pnlLookAndFeel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));

        jLabel5.setText("Look and Feel:");
        jLabel5.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel5.setMinimumSize(new java.awt.Dimension(111, 9));
        jLabel5.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        _pnlLookAndFeel.add(jLabel5);

        lookAndFeelComboBox.setModel(this.getLookAndFeelMenuItems());
        lookAndFeelComboBox.setSelectedItem(this.getInstalledLookAndFeelByClassName(_options.getLookAndFeel()).getName());
        _pnlLookAndFeel.add(lookAndFeelComboBox);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        optionsPanel.add(_pnlLookAndFeel, gridBagConstraints);

        _pnlfontChooser.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));

        jLabel8.setText("Font:");
        _pnlfontChooser.add(jLabel8);

        _fontDisplayLabel.setText("SansSerif, Plain, 10");
        _pnlfontChooser.add(_fontDisplayLabel);

        _changeFontButton.setText("...");
        _changeFontButton.setToolTipText("Choose Font");
        _changeFontButton.setAlignmentY(0.0F);
        _changeFontButton.setMargin(new java.awt.Insets(2, 3, 2, 3));
        _changeFontButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                _changeFontButtonActionPerformed(evt);
            }
        });
        _pnlfontChooser.add(_changeFontButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        optionsPanel.add(_pnlfontChooser, gridBagConstraints);

        _pnlNumClosestMatches.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));

        jLabel9.setText("Number of closest matches to return when searching:");
        jLabel9.setToolTipText("This value has memory usage implications, if you run out of memory while repairing a list, dial this down to 20 or less.");
        jLabel9.setMinimumSize(new java.awt.Dimension(257, 20));
        _pnlNumClosestMatches.add(jLabel9);

        _cbxMaxClosestMatches.setModel(new IntegerRangeComboBoxModel(10, 100));
        _cbxMaxClosestMatches.setSelectedItem(_options.getMaxClosestResults());
        _pnlNumClosestMatches.add(_cbxMaxClosestMatches);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        optionsPanel.add(_pnlNumClosestMatches, gridBagConstraints);

        _pnlRecentListLimit.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));

        jLabel1.setText("Recent Playlist Limit: ");
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel1.setMinimumSize(new java.awt.Dimension(111, 20));
        jLabel1.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        _pnlRecentListLimit.add(jLabel1);

        recentPlaylistLimitComboBox.setModel(new IntegerRangeComboBoxModel(1, 15));
        recentPlaylistLimitComboBox.setSelectedItem(_options.getMaxPlaylistHistoryEntries());
        recentPlaylistLimitComboBox.setMaximumSize(null);
        recentPlaylistLimitComboBox.setMinimumSize(null);
        recentPlaylistLimitComboBox.setPreferredSize(null);
        _pnlRecentListLimit.add(recentPlaylistLimitComboBox);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        optionsPanel.add(_pnlRecentListLimit, gridBagConstraints);

        _pnlUseUnc.setMinimumSize(new java.awt.Dimension(165, 20));
        _pnlUseUnc.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));

        jLabel6.setText("Media library uses UNC paths for directories on mapped drives:");
        jLabel6.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel6.setMinimumSize(new java.awt.Dimension(111, 20));
        jLabel6.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        _pnlUseUnc.add(jLabel6);

        alwaysUseUNCPathsCheckBox.setSelected(_options.getAlwaysUseUNCPaths());
        _pnlUseUnc.add(alwaysUseUNCPathsCheckBox);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        optionsPanel.add(_pnlUseUnc, gridBagConstraints);

        _pnlSaveRelative.setMinimumSize(new java.awt.Dimension(165, 20));
        _pnlSaveRelative.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));

        jLabel3.setText("Save playlists with relative file references:");
        jLabel3.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel3.setMinimumSize(new java.awt.Dimension(111, 20));
        jLabel3.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        _pnlSaveRelative.add(jLabel3);

        relativePathsCheckBox.setSelected(_options.getSavePlaylistsWithRelativePaths());
        _pnlSaveRelative.add(relativePathsCheckBox);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        optionsPanel.add(_pnlSaveRelative, gridBagConstraints);

        _pnlAutoLocate.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));

        jLabel2.setText("Auto-locate missing playlist entries on load:");
        jLabel2.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel2.setMinimumSize(new java.awt.Dimension(111, 20));
        jLabel2.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        _pnlAutoLocate.add(jLabel2);

        autoLocateCheckBox.setSelected(_options.getAutoLocateEntriesOnPlaylistLoad());
        _pnlAutoLocate.add(autoLocateCheckBox);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        optionsPanel.add(_pnlAutoLocate, gridBagConstraints);

        _pnRefreshMediaLibraryOnStart.setMinimumSize(new java.awt.Dimension(165, 20));
        _pnRefreshMediaLibraryOnStart.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));

        jLabel4.setText("Auto refresh media library at startup:");
        jLabel4.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel4.setMinimumSize(new java.awt.Dimension(111, 20));
        jLabel4.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        _pnRefreshMediaLibraryOnStart.add(jLabel4);

        autoRefreshOnStartupCheckBox.setSelected(_options.getAutoRefreshMediaLibraryOnStartup());
        _pnRefreshMediaLibraryOnStart.add(autoRefreshOnStartupCheckBox);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        optionsPanel.add(_pnRefreshMediaLibraryOnStart, gridBagConstraints);

        _pnlDisableCaseSensitivity.setMinimumSize(new java.awt.Dimension(165, 20));
        _pnlDisableCaseSensitivity.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));

        _lblCaseSensitivity.setText("Exact matches ignore upper/lower case:");
        _lblCaseSensitivity.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        _lblCaseSensitivity.setMinimumSize(new java.awt.Dimension(111, 20));
        _lblCaseSensitivity.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        _pnlDisableCaseSensitivity.add(_lblCaseSensitivity);

        _cbxCaseSensitivity.setSelected(_options.getCaseInsensitiveExactMatching());
        _pnlDisableCaseSensitivity.add(_cbxCaseSensitivity);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        optionsPanel.add(_pnlDisableCaseSensitivity, gridBagConstraints);

        _pnlListsDir.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));

        jLabel7.setText("Playlists Directory:");
        jLabel7.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel7.setMaximumSize(null);
        jLabel7.setMinimumSize(null);
        jLabel7.setPreferredSize(null);
        jLabel7.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        _pnlListsDir.add(jLabel7);

        playlistDirectoryTextField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        playlistDirectoryTextField.setText(_options.getPlaylistsDirectory() + TEXT_FIELD_TRAILING_SPACER);
        playlistDirectoryTextField.setMaximumSize(null);
        playlistDirectoryTextField.setMinimumSize(new java.awt.Dimension(150, 20));
        playlistDirectoryTextField.setName(""); // NOI18N
        playlistDirectoryTextField.setPreferredSize(null);
        playlistDirectoryTextField.setRequestFocusEnabled(false);
        _pnlListsDir.add(playlistDirectoryTextField);

        playlistDirectoryBrowseButton.setText("...");
        playlistDirectoryBrowseButton.setToolTipText("Browse");
        playlistDirectoryBrowseButton.setMargin(new java.awt.Insets(2, 3, 2, 3));
        playlistDirectoryBrowseButton.setName(""); // NOI18N
        playlistDirectoryBrowseButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                playlistDirectoryBrowseButtonActionPerformed(evt);
            }
        });
        _pnlListsDir.add(playlistDirectoryBrowseButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        optionsPanel.add(_pnlListsDir, gridBagConstraints);

        _pnlSmallWords.setMinimumSize(new java.awt.Dimension(165, 20));
        _pnlSmallWords.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));

        jLabel10.setText("Words to ignore while finding closest matches:");
        jLabel10.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel10.setMinimumSize(new java.awt.Dimension(111, 9));
        jLabel10.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        _pnlSmallWords.add(jLabel10);

        _smallWordsTxtField.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        _smallWordsTxtField.setText(_options.getIgnoredSmallWords() + TEXT_FIELD_TRAILING_SPACER);
        _smallWordsTxtField.setMinimumSize(new java.awt.Dimension(150, 20));
        _smallWordsTxtField.setPreferredSize(null);
        _pnlSmallWords.add(_smallWordsTxtField);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 3, 0);
        optionsPanel.add(_pnlSmallWords, gridBagConstraints);

        topPanel.add(optionsPanel);

        buttonPanel.setMaximumSize(null);
        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jButton1.setText("OK");
        jButton1.setMinimumSize(new java.awt.Dimension(49, 20));
        jButton1.setPreferredSize(null);
        jButton1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton1ActionPerformed(evt);
            }
        });
        buttonPanel.add(jButton1);

        jButton2.setText("Cancel");
        jButton2.setMinimumSize(new java.awt.Dimension(67, 20));
        jButton2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jButton2ActionPerformed(evt);
            }
        });
        buttonPanel.add(jButton2);

        topPanel.add(buttonPanel);

        getContentPane().add(topPanel);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
    setVisible(false);
    dispose();
    setResultCode(CANCEL);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    setVisible(false);
    dispose();
    setResultCode(OK);
    }//GEN-LAST:event_jButton1ActionPerformed

  /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
    setVisible(false);
    dispose();
    }//GEN-LAST:event_closeDialog

  private void playlistDirectoryBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playlistDirectoryBrowseButtonActionPerformed
    if (_options.getPlaylistsDirectory() != null && _options.getPlaylistsDirectory().length() > 0)
    {
      _jMediaDirChooser.setCurrentDirectory(new File(_options.getPlaylistsDirectory()));
      _jMediaDirChooser.setSelectedFolder(new File(_options.getPlaylistsDirectory()));
    }
    int response = _jMediaDirChooser.showOpenDialog(this);
    if (response == JFileChooser.APPROVE_OPTION)
    {
      try
      {
        String path = _jMediaDirChooser.getSelectedFile().getPath();
        if (new File(path).exists())
        {
          playlistDirectoryTextField.setText(path + TEXT_FIELD_TRAILING_SPACER);
        }
        else
        {
          throw new FileNotFoundException();
        }
      }
      catch (FileNotFoundException e)
      {
        JOptionPane.showMessageDialog(this, new JTransparentTextArea("The directory you selected/entered does not exist."));
        _logger.info(ExStack.toString(e));
      }
    }
  }//GEN-LAST:event_playlistDirectoryBrowseButtonActionPerformed

  private void _changeFontButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event__changeFontButtonActionPerformed
  {//GEN-HEADEREND:event__changeFontButtonActionPerformed
    JFontChooser jfc = new JFontChooser();
    jfc.setSelectedFont(_chosenFont);
    jfc.showDialog(this);
    _chosenFont = jfc.getSelectedFont();
    _fontDisplayLabel.setText(FontExtensions.formatFont(_chosenFont));
  }//GEN-LAST:event__changeFontButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox _cbxCaseSensitivity;
    private javax.swing.JComboBox _cbxMaxClosestMatches;
    private javax.swing.JButton _changeFontButton;
    private javax.swing.JLabel _fontDisplayLabel;
    private javax.swing.JLabel _lblCaseSensitivity;
    private javax.swing.JPanel _pnRefreshMediaLibraryOnStart;
    private javax.swing.JPanel _pnlAutoLocate;
    private javax.swing.JPanel _pnlDisableCaseSensitivity;
    private javax.swing.JPanel _pnlListsDir;
    private javax.swing.JPanel _pnlLookAndFeel;
    private javax.swing.JPanel _pnlNumClosestMatches;
    private javax.swing.JPanel _pnlRecentListLimit;
    private javax.swing.JPanel _pnlSaveRelative;
    private javax.swing.JPanel _pnlSmallWords;
    private javax.swing.JPanel _pnlUseUnc;
    private javax.swing.JPanel _pnlfontChooser;
    private javax.swing.JTextField _smallWordsTxtField;
    private javax.swing.JCheckBox alwaysUseUNCPathsCheckBox;
    private javax.swing.JCheckBox autoLocateCheckBox;
    private javax.swing.JCheckBox autoRefreshOnStartupCheckBox;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JComboBox lookAndFeelComboBox;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JButton playlistDirectoryBrowseButton;
    private javax.swing.JTextField playlistDirectoryTextField;
    private javax.swing.JComboBox recentPlaylistLimitComboBox;
    private javax.swing.JCheckBox relativePathsCheckBox;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables
}
