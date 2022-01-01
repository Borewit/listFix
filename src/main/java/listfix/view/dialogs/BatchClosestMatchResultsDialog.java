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

import java.awt.DisplayMode;


import java.util.List;

import javax.swing.table.TableCellEditor;

import listfix.model.BatchMatchItem;
import listfix.view.controls.ClosestMatchesSearchScrollableResultsPanel;

import org.apache.log4j.Logger;

/**
 * This is the results dialog we display when running a closest matches search on multiple files in a playlist (this operation originally worked only on a single entry)..
 * @author jcaron
 */
public class BatchClosestMatchResultsDialog extends javax.swing.JDialog
{
  private static final Logger _logger = Logger.getLogger(BatchClosestMatchResultsDialog.class);

  /**
   *
   * @param parent
   * @param items
   */
  public BatchClosestMatchResultsDialog(java.awt.Frame parent, List<BatchMatchItem> items)
  {
    super(parent, true);
    _items = items;
    initComponents();

    // get screenwidth using workaround for vmware/linux bug
        int screenWidth;
        DisplayMode dmode = getGraphicsConfiguration().getDevice().getDisplayMode();
        if (dmode != null)
    {
            screenWidth = dmode.getWidth();
    }
        else
    {
            screenWidth = getGraphicsConfiguration().getBounds().width;
    }
        int newWidth = _pnlResults.getTableWidth() + getWidth() - _pnlResults.getWidth() + 2;
        setSize(Math.min(newWidth, screenWidth - 50), getHeight());
  }

  /**
   *
   * @return
   */
  public boolean isAccepted()
  {
    return _isAccepted;
  }
  private boolean _isAccepted;

  /**
   *
   * @return
   */
  public List<BatchMatchItem> getMatches()
  {
    return _items;
  }
  private List<BatchMatchItem> _items;

  /** This method is called from within the constructor to
   * initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is
   * always regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        _btnOk = new javax.swing.JButton();
        _btnCancel = new javax.swing.JButton();
        _pnlResults = new ClosestMatchesSearchScrollableResultsPanel(_items);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Select Closest Matches");

        jPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        _btnOk.setText("OK");
        _btnOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onBtnOkActionPerformed(evt);
            }
        });
        jPanel1.add(_btnOk);

        _btnCancel.setText("Cancel");
        _btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onBtnCancelActionPerformed(evt);
            }
        });
        jPanel1.add(_btnCancel);

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);
        getContentPane().add(_pnlResults, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void onBtnOkActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onBtnOkActionPerformed
    {//GEN-HEADEREND:event_onBtnOkActionPerformed
    _isAccepted = true;
    if (_pnlResults.getSelectedRow() > -1 && _pnlResults.getSelectedColumn() == 3)
    {
      TableCellEditor cellEditor = _pnlResults.getCellEditor(_pnlResults.getSelectedRow(), _pnlResults.getSelectedColumn());
      cellEditor.stopCellEditing();
    }
    setVisible(false);
    }//GEN-LAST:event_onBtnOkActionPerformed

    private void onBtnCancelActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_onBtnCancelActionPerformed
    {//GEN-HEADEREND:event_onBtnCancelActionPerformed
    _isAccepted = false;
    setVisible(false);
    }//GEN-LAST:event_onBtnCancelActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton _btnCancel;
    private javax.swing.JButton _btnOk;
    private listfix.view.controls.ClosestMatchesSearchScrollableResultsPanel _pnlResults;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

}
