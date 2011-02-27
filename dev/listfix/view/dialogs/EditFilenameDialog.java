/*
 * listFix() - Fix Broken Playlists!
 * Copyright (C) 2001-2010 Jeremy Caron
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
import listfix.model.EditFilenameResult;
import java.awt.Point;

public class EditFilenameDialog extends javax.swing.JDialog
{
	private static final long serialVersionUID = 1825443635993970657L;
	public static final int OK = 0;
	public static final int CANCEL = 1;
	private int resultCode = CANCEL;
	private String fileName;

	/** Creates new form EditFilenameDialog */
	public EditFilenameDialog(java.awt.Frame parent, String title, boolean modal, String filename)
	{
		super(parent, title, modal);
		initComponents();
		jTextField1.setText(filename);
		this.center();
	}

	public EditFilenameDialog()
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

	private void center()
	{
		Point parentLocation = this.getParent().getLocationOnScreen();
		double x = parentLocation.getX();
		double y = parentLocation.getY();
		int width = this.getParent().getWidth();
		int height = this.getParent().getHeight();

		this.setLocation((int) x + (width - this.getWidth()) / 2, (int) y + (height - this.getHeight()) / 2);
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel2.setFont(new java.awt.Font("SansSerif", 0, 10));
        jLabel2.setText("If you renamed a file by hand");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        jPanel1.add(jLabel2, gridBagConstraints);

        jLabel3.setFont(new java.awt.Font("SansSerif", 0, 10));
        jLabel3.setText("and know the new filename, ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel1.add(jLabel3, gridBagConstraints);

        jLabel4.setFont(new java.awt.Font("SansSerif", 0, 10));
        jLabel4.setText("you can enter it below.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        jPanel1.add(jLabel4, gridBagConstraints);

        getContentPane().add(jPanel1);

        jPanel2.setMaximumSize(null);

        jLabel1.setFont(new java.awt.Font("SansSerif", 0, 10));
        jLabel1.setText("New Filename:");
        jPanel2.add(jLabel1);

        jTextField1.setFont(new java.awt.Font("SansSerif", 0, 10));
        jTextField1.setMinimumSize(new java.awt.Dimension(150, 20));
        jTextField1.setPreferredSize(new java.awt.Dimension(250, 20));
        jPanel2.add(jTextField1);

        getContentPane().add(jPanel2);

        jPanel3.setMaximumSize(null);

        jButton1.setFont(new java.awt.Font("SansSerif", 0, 10));
        jButton1.setText("OK");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton1);

        jButton2.setFont(new java.awt.Font("SansSerif", 0, 10));
        jButton2.setText("Cancel");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton2);

        getContentPane().add(jPanel3);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
		setVisible(false);
		dispose();
		setResultCode(CANCEL);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
		setVisible(false);
		setFileName(jTextField1.getText());
		dispose();
		setResultCode(OK);
    }//GEN-LAST:event_jButton1ActionPerformed

	/** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
		setVisible(false);
		dispose();
    }//GEN-LAST:event_closeDialog

	public static EditFilenameResult showDialog(java.awt.Frame parent, String title, boolean modal, String filename)
	{
		EditFilenameDialog tempDBox = new EditFilenameDialog(parent, title, modal, filename);
		tempDBox.setVisible(true);
		return new EditFilenameResult(tempDBox.getResultCode(), tempDBox.getFileName());
	}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
