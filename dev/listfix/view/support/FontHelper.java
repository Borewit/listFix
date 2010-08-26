/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package listfix.view.support;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;

/**
 *
 * @author jcaron
 */
public class FontHelper
{
	public static void setFileChooserFont(Component[] comp)
	{
		for (int x = 0; x < comp.length; x++)
		{
			if (comp[x] instanceof Container)
			{
				setFileChooserFont(((Container) comp[x]).getComponents());
			}
			try
			{
				comp[x].setFont(new Font("Verdana", 0, 9));
			}
			catch (Exception e)
			{
				// keep going...
			}
		}
	}
}
