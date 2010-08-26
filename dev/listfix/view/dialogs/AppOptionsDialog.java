/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2009 Jeremy Caron
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
import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import listfix.model.AppOptions;
import listfix.view.support.FontHelper;

public class AppOptionsDialog extends javax.swing.JDialog
{
	private static final long serialVersionUID = 3409894354485158935L;
	public static final int OK = 0;
	public static final int CANCEL = 1;
	private int resultCode;
	private String fileName;
	private AppOptions options = null;
	private final JFileChooser jMediaDirChooser = new JFileChooser();

	/** Creates new form EditFilenameDialog */
	public AppOptionsDialog(java.awt.Frame parent, String title, boolean modal, AppOptions opts)
	{
		super(parent, title, modal);
		if (opts == null)
		{
			options = new AppOptions();
		}
		else
		{
			options = opts;
		}
		this.setPreferredSize(new Dimension(390, 292));
		this.center();
		initComponents();
		jMediaDirChooser.setDialogTitle("Specify a playlists directory...");
		jMediaDirChooser.setAcceptAllFileFilterUsed(false);
		jMediaDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		FontHelper.setFileChooserFont(jMediaDirChooser.getComponents());
	}

	public AppOptionsDialog()
	{
	}

	public String getFileName()
	{
		return fileName;
	}

	public void setFileName(String x)
	{
		fileName = x;
	}

	public void setResultCode(int i)
	{
		resultCode = i;
	}

	public int getResultCode()
	{
		return resultCode;
	}

	private LookAndFeelInfo[] getInstalledLookAndFeels()
	{
		LookAndFeelInfo[] plafs = UIManager.getInstalledLookAndFeels();
		List<LookAndFeelInfo> lafs = new ArrayList<LookAndFeelInfo>();
		for (LookAndFeelInfo laf : plafs)
		{
			if (!laf.getName().toLowerCase().contains("nimbus"))
			{
				lafs.add(laf);
			}
		}
		plafs = lafs.toArray(new LookAndFeelInfo[0]);
		return plafs;
	}

	private DefaultComboBoxModel getLookAndFeelMenuItems()
	{
		LookAndFeelInfo[] plafs = getInstalledLookAndFeels();

		String[] model = new String[plafs.length];
		for (int i = 0; i < plafs.length; i++)
		{
			model[i] = plafs[i].getName();
		}
		return new DefaultComboBoxModel(model);
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
		for (int i = 0; i < plafs.length; i++)
		{
			if (name.equals(plafs[i].getClassName()))
			{
				return plafs[i];
			}
		}
		return plafs[0];
	}

	private void center()
	{
		Point parentLocation = this.getParent().getLocationOnScreen();
		double x = parentLocation.getX();
		double y = parentLocation.getY();
		int width = this.getParent().getWidth();
		int height = this.getParent().getHeight();

		this.setLocation((int) x + (width - this.getPreferredSize().width) / 2, (int) y + (height - this.getPreferredSize().height) / 2);
//        Frame parent = (Frame)getParent();
//        Dimension dim = parent.getSize();
//        Point     loc = parent.getLocationOnScreen();
//
//        Dimension size = getSize();
//
//        loc.x += (dim.width  - size.width)/2;
//        loc.y += (dim.height - size.height)/2;
//
//        if (loc.x < 0) loc.x = 0;
//        if (loc.y < 0) loc.y = 0;
//
//        Dimension screen = getToolkit().getScreenSize();
//
//        if (size.width  > screen.width)
//          size.width  = screen.width;
//        if (size.height > screen.height)
//          size.height = screen.height;
//
//        if (loc.x + size.width > screen.width)
//          loc.x = screen.width - size.width;
//
//        if (loc.y + size.height > screen.height)
//          loc.y = screen.height - size.height;
//
//        setBounds(loc.x, loc.y, size.width, size.height);
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        topPanel = new javax.swing.JPanel();
        optionsPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        recentPlaylistLimitComboBox = new javax.swing.JComboBox();
        jPanel8 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        lookAndFeelComboBox = new javax.swing.JComboBox();
        jPanel4 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        autoLocateCheckBox = new javax.swing.JCheckBox();
        jPanel5 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        relativePathsCheckBox = new javax.swing.JCheckBox();
        jPanel7 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        autoRefreshOnStartupCheckBox = new javax.swing.JCheckBox();
        jPanel9 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        alwaysUseUNCPathsCheckBox = new javax.swing.JCheckBox();
        jPanel6 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        playlistDirectoryTextField = new javax.swing.JTextField();
        playlistDirectoryBrowseButton = new javax.swing.JButton();
        buttonPanel = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setMinimumSize(new java.awt.Dimension(430, 320));
        setModal(true);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        topPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        topPanel.setLayout(new javax.swing.BoxLayout(topPanel, javax.swing.BoxLayout.Y_AXIS));

        optionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "General Config", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Verdana", 0, 9))); // NOI18N
        optionsPanel.setMinimumSize(new java.awt.Dimension(1210, 60));
        optionsPanel.setPreferredSize(new java.awt.Dimension(380, 280));
        optionsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));

        jLabel1.setFont(new java.awt.Font("Verdana", 0, 9));
        jLabel1.setText("Recent Playlist Limit: ");
        jLabel1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel1.setMinimumSize(new java.awt.Dimension(111, 9));
        jLabel1.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        jPanel3.add(jLabel1);

        recentPlaylistLimitComboBox.setFont(new java.awt.Font("Verdana", 0, 9));
        recentPlaylistLimitComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15" }));
        recentPlaylistLimitComboBox.setSelectedItem("" + options.getMaxPlaylistHistoryEntries());
        recentPlaylistLimitComboBox.setPreferredSize(new java.awt.Dimension(50, 20));
        jPanel3.add(recentPlaylistLimitComboBox);

        optionsPanel.add(jPanel3);

        jPanel8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));

        jLabel5.setFont(new java.awt.Font("Verdana", 0, 9));
        jLabel5.setText("Look and Feel:");
        jLabel5.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel5.setMinimumSize(new java.awt.Dimension(111, 9));
        jLabel5.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        jPanel8.add(jLabel5);

        lookAndFeelComboBox.setFont(new java.awt.Font("Verdana", 0, 9));
        lookAndFeelComboBox.setModel(this.getLookAndFeelMenuItems());
        lookAndFeelComboBox.setSelectedItem(this.getInstalledLookAndFeelByClassName(options.getLookAndFeel()).getName());
        lookAndFeelComboBox.setPreferredSize(new java.awt.Dimension(120, 20));
        jPanel8.add(lookAndFeelComboBox);

        optionsPanel.add(jPanel8);

        jPanel4.setMinimumSize(new java.awt.Dimension(165, 20));
        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));

        jLabel2.setFont(new java.awt.Font("Verdana", 0, 9));
        jLabel2.setText("Auto-locate missing playlist entries on load:");
        jLabel2.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel2.setMinimumSize(new java.awt.Dimension(111, 9));
        jLabel2.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        jPanel4.add(jLabel2);

        autoLocateCheckBox.setSelected(options.getAutoLocateEntriesOnPlaylistLoad());
        jPanel4.add(autoLocateCheckBox);

        optionsPanel.add(jPanel4);

        jPanel5.setMinimumSize(new java.awt.Dimension(165, 20));
        jPanel5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));

        jLabel3.setFont(new java.awt.Font("Verdana", 0, 9));
        jLabel3.setText("Save playlists with relative file references:");
        jLabel3.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel3.setMinimumSize(new java.awt.Dimension(111, 9));
        jLabel3.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        jPanel5.add(jLabel3);

        relativePathsCheckBox.setSelected(options.getSavePlaylistsWithRelativePaths());
        jPanel5.add(relativePathsCheckBox);

        optionsPanel.add(jPanel5);

        jPanel7.setMinimumSize(new java.awt.Dimension(165, 20));
        jPanel7.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));

        jLabel4.setFont(new java.awt.Font("Verdana", 0, 9));
        jLabel4.setText("Auto refresh media library at startup:");
        jLabel4.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel4.setMinimumSize(new java.awt.Dimension(111, 9));
        jLabel4.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        jPanel7.add(jLabel4);

        autoRefreshOnStartupCheckBox.setSelected(options.getAutoRefreshMediaLibraryOnStartup());
        jPanel7.add(autoRefreshOnStartupCheckBox);

        optionsPanel.add(jPanel7);

        jPanel9.setMinimumSize(new java.awt.Dimension(165, 20));
        jPanel9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));

        jLabel6.setFont(new java.awt.Font("Verdana", 0, 9));
        jLabel6.setText("Media library uses UNC paths for directories on mapped drives:");
        jLabel6.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel6.setMinimumSize(new java.awt.Dimension(111, 9));
        jLabel6.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        jPanel9.add(jLabel6);

        alwaysUseUNCPathsCheckBox.setSelected(options.getAlwaysUseUNCPaths());
        jPanel9.add(alwaysUseUNCPathsCheckBox);

        optionsPanel.add(jPanel9);

        jPanel6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));

        jLabel7.setFont(new java.awt.Font("Verdana", 0, 9));
        jLabel7.setText("Playlists Directory:");
        jLabel7.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel7.setMinimumSize(new java.awt.Dimension(111, 9));
        jLabel7.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        jPanel6.add(jLabel7);

        playlistDirectoryTextField.setFont(new java.awt.Font("Verdana", 0, 9));
        playlistDirectoryTextField.setText(options.getPlaylistsDirectory());
        playlistDirectoryTextField.setAlignmentX(0.0F);
        playlistDirectoryTextField.setAlignmentY(0.0F);
        playlistDirectoryTextField.setMaximumSize(null);
        playlistDirectoryTextField.setPreferredSize(new java.awt.Dimension(200, 20));
        playlistDirectoryTextField.setRequestFocusEnabled(false);
        jPanel6.add(playlistDirectoryTextField);

        playlistDirectoryBrowseButton.setFont(new java.awt.Font("Verdana", 0, 9));
        playlistDirectoryBrowseButton.setText("...");
        playlistDirectoryBrowseButton.setAlignmentY(0.0F);
        playlistDirectoryBrowseButton.setMargin(new java.awt.Insets(2, 6, 2, 6));
        playlistDirectoryBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playlistDirectoryBrowseButtonActionPerformed(evt);
            }
        });
        jPanel6.add(playlistDirectoryBrowseButton);

        optionsPanel.add(jPanel6);

        topPanel.add(optionsPanel);

        buttonPanel.setMaximumSize(new java.awt.Dimension(400, 24));
        buttonPanel.setMinimumSize(new java.awt.Dimension(131, 24));
        buttonPanel.setPreferredSize(new java.awt.Dimension(350, 32));
        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jButton1.setFont(new java.awt.Font("Verdana", 0, 9));
        jButton1.setText("OK");
        jButton1.setMinimumSize(new java.awt.Dimension(49, 20));
        jButton1.setPreferredSize(null);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        buttonPanel.add(jButton1);

        jButton2.setFont(new java.awt.Font("Verdana", 0, 9));
        jButton2.setText("Cancel");
        jButton2.setMinimumSize(new java.awt.Dimension(67, 20));
        jButton2.setPreferredSize(null);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
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
		int response = jMediaDirChooser.showOpenDialog(this);
		if (response == JFileChooser.APPROVE_OPTION)
		{
			try
			{
				String path = jMediaDirChooser.getSelectedFile().getPath();
				if (new File(path).exists())
				{
					playlistDirectoryTextField.setText(path);
				}
				else
				{
					throw new FileNotFoundException();
				}
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(this, "The directory you selected/entered does not exist.");
				e.printStackTrace();
			}
		}
	}//GEN-LAST:event_playlistDirectoryBrowseButtonActionPerformed

	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[])
	{
		new AppOptionsDialog(new java.awt.Frame(), "listFix() options", true, null).setVisible(true);
	}

	public AppOptions showDialog()
	{
		this.setVisible(true);
		if (this.getResultCode() == OK)
		{
			options.setAutoLocateEntriesOnPlaylistLoad(autoLocateCheckBox.isSelected());
			options.setMaxPlaylistHistoryEntries(new Integer((String) recentPlaylistLimitComboBox.getItemAt(recentPlaylistLimitComboBox.getSelectedIndex())).intValue());
			options.setSavePlaylistsWithRelativePaths(relativePathsCheckBox.isSelected());
			options.setAutoRefreshMediaLibraryOnStartup(autoRefreshOnStartupCheckBox.isSelected());
			options.setLookAndFeel(this.getInstalledLookAndFeelAtIndex(lookAndFeelComboBox.getSelectedIndex()).getClassName());
			options.setAlwaysUseUNCPaths(this.alwaysUseUNCPathsCheckBox.isSelected());
			options.setPlaylistsDirectory(playlistDirectoryTextField.getText());
		}
		return options;
	}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox alwaysUseUNCPathsCheckBox;
    private javax.swing.JCheckBox autoLocateCheckBox;
    private javax.swing.JCheckBox autoRefreshOnStartupCheckBox;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JComboBox lookAndFeelComboBox;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JButton playlistDirectoryBrowseButton;
    private javax.swing.JTextField playlistDirectoryTextField;
    private javax.swing.JComboBox recentPlaylistLimitComboBox;
    private javax.swing.JCheckBox relativePathsCheckBox;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables
}
