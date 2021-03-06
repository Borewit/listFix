/*
 * listFix() - Fix Broken Playlists!
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

import listfix.model.playlists.Playlist;

/**
 *
 * @author jcaron
 */
public class ReorderPlaylistDialog extends javax.swing.JDialog
{

/** Creates new form ReorderPlaylistDialog
 * @param parent
 * @param modal
 * @param sortIx
 * @param isDescending
 */
  public ReorderPlaylistDialog(java.awt.Frame parent, boolean modal, Playlist.SortIx sortIx, boolean isDescending)
  {
    super(parent, modal);
      initComponents();

      switch (sortIx)
      {
        case None:
          _rbReverse.setSelected(true);
          break;

        case Filename:
          _rbFilename.doClick();
          _chkDescending.setSelected(isDescending);
          break;

        case Path:
          _rbPath.doClick();
          _chkDescending.setSelected(isDescending);
          break;

        case Status:
          _rbStatus.doClick();
          _chkDescending.setSelected(isDescending);
          break;

        default:
          _rbRandom.setSelected(true);
    }
  }

    /**
   *
   * @return
   */
  public Playlist.SortIx getSelectedSortIx()
  {
    if (_wasCancelled)
      return Playlist.SortIx.None;
    else if (_rbRandom.isSelected())
      return Playlist.SortIx.Random;
    else if (_rbReverse.isSelected())
      return Playlist.SortIx.Reverse;
    else if (_rbFilename.isSelected())
      return Playlist.SortIx.Filename;
    else if (_rbPath.isSelected())
      return Playlist.SortIx.Path;
    else if (_rbStatus.isSelected())
      return Playlist.SortIx.Status;
    else
      return Playlist.SortIx.None;
  }

  boolean _wasCancelled;

    /**
   *
   * @return
   */
  public boolean getIsDescending()
  {
      return _chkDescending.isSelected();
  }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        _rbRandom = new javax.swing.JRadioButton();
        _rbReverse = new javax.swing.JRadioButton();
        _rbFilename = new javax.swing.JRadioButton();
        _rbPath = new javax.swing.JRadioButton();
        _rbStatus = new javax.swing.JRadioButton();
        _chkDescending = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Reorder Playlist");
        setMinimumSize(new java.awt.Dimension(300, 189));
        setModal(true);
        setResizable(false);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Select the new playlist order"));
        jPanel1.setAlignmentX(1.0F);
        jPanel1.setLayout(new java.awt.GridLayout(6, 1));

        buttonGroup1.add(_rbRandom);
        _rbRandom.setText("Randomized");
        _rbRandom.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                onUnsortedOptionSelected(evt);
            }
        });
        jPanel1.add(_rbRandom);

        buttonGroup1.add(_rbReverse);
        _rbReverse.setText("Reversed");
        _rbReverse.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                onUnsortedOptionSelected(evt);
            }
        });
        jPanel1.add(_rbReverse);

        buttonGroup1.add(_rbFilename);
        _rbFilename.setText("By Filename");
        _rbFilename.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                onSortedOptionSelected(evt);
            }
        });
        jPanel1.add(_rbFilename);

        buttonGroup1.add(_rbPath);
        _rbPath.setText("By Location");
        _rbPath.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                onSortedOptionSelected(evt);
            }
        });
        jPanel1.add(_rbPath);

        buttonGroup1.add(_rbStatus);
        _rbStatus.setText("By Status");
        _rbStatus.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                onSortedOptionSelected(evt);
            }
        });
        jPanel1.add(_rbStatus);

        _chkDescending.setText("Descending");
        _chkDescending.setEnabled(false);
        jPanel1.add(_chkDescending);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        jButton1.setText("Proceed");
        jButton1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                onBtnOkActionPerformed(evt);
            }
        });
        jPanel2.add(jButton1);

        jButton2.setText("Cancel");
        jButton2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                onBtnCancelActionPerformed(evt);
            }
        });
        jPanel2.add(jButton2);

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

  private void onBtnCancelActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onBtnCancelActionPerformed
  {//GEN-HEADEREND:event_onBtnCancelActionPerformed
    _wasCancelled = true;
    setVisible(false);
  }//GEN-LAST:event_onBtnCancelActionPerformed

  private void onBtnOkActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onBtnOkActionPerformed
  {//GEN-HEADEREND:event_onBtnOkActionPerformed
    setVisible(false);
  }//GEN-LAST:event_onBtnOkActionPerformed

  private void onUnsortedOptionSelected(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onUnsortedOptionSelected
  {//GEN-HEADEREND:event_onUnsortedOptionSelected
    _chkDescending.setEnabled(false);
  }//GEN-LAST:event_onUnsortedOptionSelected

  private void onSortedOptionSelected(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onSortedOptionSelected
  {//GEN-HEADEREND:event_onSortedOptionSelected
    _chkDescending.setEnabled(true);
  }//GEN-LAST:event_onSortedOptionSelected

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JCheckBox _chkDescending;
  private javax.swing.JRadioButton _rbFilename;
  private javax.swing.JRadioButton _rbPath;
  private javax.swing.JRadioButton _rbRandom;
  private javax.swing.JRadioButton _rbReverse;
  private javax.swing.JRadioButton _rbStatus;
  private javax.swing.ButtonGroup buttonGroup1;
  private javax.swing.JButton jButton1;
  private javax.swing.JButton jButton2;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  // End of variables declaration//GEN-END:variables
}
