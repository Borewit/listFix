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

import javax.swing.*;
import java.awt.*;

public class SplashScreen extends JFrame
{
  private final JLabel statusBar;

  public SplashScreen(ImageIcon image)
  {
    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(new JLabel(image));

    statusBar = new JLabel(" ");
    statusBar.setBorder(BorderFactory.createBevelBorder(1));
    getContentPane().add(statusBar, BorderLayout.SOUTH);

    setUndecorated(true);
    pack();

    DisplayMode dm = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
    setBounds(dm.getWidth() / 2 - getWidth() / 2, dm.getHeight() / 2 - getHeight() / 2, getWidth(), getHeight());

    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
  }


  public void setStatusBar(String text)
  {
    statusBar.setText(text);
  }
}
