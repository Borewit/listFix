/* $Id: SplashScreen.java,v 1.5 2003/12/16 00:09:55 JasonMichalski Exp $
 *
 * Copyright (C) 2003  Jason Michalski armooo@armooo.net
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * icense as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package listfix.view.support;

import java.awt.*;

import javax.swing.*;

public class SplashScreen extends JFrame
{
	private JLabel statusBar;

	public SplashScreen(String imageResourcePath)
	{

		ClassLoader cl = this.getClass().getClassLoader();
		ImageIcon image = new ImageIcon(cl.getResource(imageResourcePath));

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(new JLabel(image));

		statusBar = new JLabel(" ");
		statusBar.setBorder(BorderFactory.createBevelBorder(1));
		statusBar.setFont(new Font("Verdana", 0, 9));
		getContentPane().add(statusBar, BorderLayout.SOUTH);

		setUndecorated(true);

		pack();

		Toolkit toolKit = getToolkit();
		Dimension scrSize = toolKit.getScreenSize();

		setBounds(scrSize.width / 2 - getWidth() / 2, scrSize.height / 2 - getHeight() / 2, getWidth(), getHeight());

		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		setVisible(true);
	}

	public void setStatusBar(String text)
	{
		statusBar.setText(text);
	}
}
