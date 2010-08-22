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

package listfix.view.support;

import listfix.controller.Task;
import java.awt.*;

import javax.swing.*;

public class ProgressPopup extends JDialog implements IProgressObserver
{
	private static final long serialVersionUID = -7397347218289763336L;
	private JProgressBar bar;
	private JLabel label, iconLabel;
	private JPanel _main;

	protected JPanel getMainContainer()
	{
		return (_main);
	}

	@Override
	public void setVisible(boolean flag)
	{
		if (flag)
		{
			startFocus();
		}

		super.setVisible(flag);
	}

	protected void startFocus()
	{
	}

	public void setBusyCursor(boolean flag)
	{
		setCursor(Cursor.getPredefinedCursor(flag ? Cursor.WAIT_CURSOR
			: Cursor.DEFAULT_CURSOR));
	}

	protected boolean canClose()
	{
		return (true);
	}

	public ProgressPopup(Frame parent, String title, boolean modal, int labelWidth, int labelHeight, boolean messageOnlyMode) //, boolean supportsCancel)
	{
		super(parent, title, modal);
		_init();

		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		JPanel main = this.getMainContainer();

		main.setLayout(new BorderLayout(5, 5));

		main.add("North", label = new JLabel("Please Wait"));
		label.setPreferredSize(new Dimension(labelWidth, labelHeight));
		label.setFont(new Font("Verdana", 0, 9));
		label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		bar = new JProgressBar();

		if (!messageOnlyMode)
		{
			JPanel p = new JPanel();
			p.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

			bar.setMinimum(0);
			bar.setMaximum(100);
			bar.setValue(0);
			bar.setOpaque(false);
			bar.setPreferredSize(new java.awt.Dimension(main.getPreferredSize().width, bar.getPreferredSize().height));
			p.add(bar);

			main.add("Center", p);
		}

		if (getTitle().length() == 0)
		{
			setTitle(title);
		}

		pack();
	}

	public void reportProgress(int progress)
	{
		if (progress < 0)
		{
			progress = 0;
		}
		else if (progress > 100)
		{
			progress = 100;
		}

		bar.setValue(progress);
		if (progress == 100)
		{
			this.paintImmediately(bar);
			this.sleep(1);
			setVisible(false);
		}
	}

    public void reportProgress(int progress, Object state)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

	private void paintImmediately(Component c)
	{
		Graphics gc = c.getGraphics();
		if (gc != null)
		{
			c.paint(gc);
			gc.dispose();
		}
	}

	private void sleep(int sec)
	{
		try
		{
			Thread.sleep(sec * 1000);
		}
		catch (InterruptedException ex)
		{
		}
	}

	public void setIcon(Icon icon)
	{
		iconLabel.setIcon(icon);
	}

	public void setMessage(String message)
	{
		label.setText(message);
		pack();
	}

	public void track(Task task)
	{
		bar.setValue(0);
		task.addProgressObserver(this);
		Thread t = new Thread(task);
		t.start();
		setVisible(true);
		task.removeProgressObserver(this);
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

	public void go()
	{
		this.center();
		this.setEnabled(true);
	}

	private void _init()
	{
		getContentPane().setLayout(new GridLayout(1, 0));
		_main = new JPanel();
		_main.setOpaque(true);
		getContentPane().add(_main);
	}
}
