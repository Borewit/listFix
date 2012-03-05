/*
 *  listFix() - Fix Broken Playlists!
 *  Copyright (C) 2001-2010 Jeremy Caron
 * 
 *  This file is part of listFix().
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, please see http://www.gnu.org/licenses/
 */

package listfix.view.support;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.awt.image.BufferedImage;
import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.TransferHandler;

/**
 *
 * @author jcaron
 */
public class TabTransferHandler extends TransferHandler
{
	private final DataFlavor localObjectFlavor;

	public TabTransferHandler()
	{
		System.out.println("TabTransferHandler");
		localObjectFlavor = new ActivationDataFlavor(DnDTabbedPane.class, DataFlavor.javaJVMLocalObjectMimeType, "DnDTabbedPane");
	}
//     private static DnDTabbedPane source;
//     private synchronized static void setComponent(JComponent comp) {
//         if(comp instanceof DnDTabbedPane) {
//             source = (DnDTabbedPane)comp;
//         }
//     }
//     @Override public void exportAsDrag(JComponent comp, InputEvent e, int action) {
//         super.exportAsDrag(comp, e, action);
//         setComponent(comp);
//     }
	private DnDTabbedPane source = null;

	@Override
	protected Transferable createTransferable(JComponent c)
	{
		System.out.println("createTransferable");
		if (c instanceof DnDTabbedPane)
		{
			source = (DnDTabbedPane) c;
		}
		return new DataHandler(c, localObjectFlavor.getMimeType());
	}

	@Override
	public boolean canImport(TransferSupport support)
	{
		//System.out.println("canImport");
		if (!support.isDrop() || !support.isDataFlavorSupported(localObjectFlavor))
		{
			System.out.println("canImport:" + support.isDrop() + " " + support.isDataFlavorSupported(localObjectFlavor));
			return false;
		}
		support.setDropAction(MOVE);
		DropLocation tdl = support.getDropLocation();
		Point pt = tdl.getDropPoint();
		DnDTabbedPane target = (DnDTabbedPane) support.getComponent();
		target.autoScrollTest(pt);
		DnDTabbedPane.DropLocation dl = (DnDTabbedPane.DropLocation) target.dropLocationForPoint(pt);
		int idx = dl.getIndex();
		boolean isDropable = false;

//         DnDTabbedPane source = TabTransferHandler.source;
//         if(!isWebStart()) {
//             try{
//                 source = (DnDTabbedPane)support.getTransferable().getTransferData(localObjectFlavor);
//             }catch(Exception ex) {
//                 ex.printStackTrace();
//             }
//         }
		if (target == source)
		{
			//System.out.println("target==source");
			isDropable = target.getTabAreaBounds().contains(pt) && idx >= 0 && idx != target.dragTabIndex && idx != target.dragTabIndex + 1;
		}
		else
		{
			//System.out.format("target!=source\n  target: %s\n  source: %s", target.getName(), source.getName());
			if (source != null && target != source.getComponentAt(source.dragTabIndex))
			{
				isDropable = target.getTabAreaBounds().contains(pt) && idx >= 0;
			}
		}
		//if(glassPane!=target.getRootPane().getGlassPane()) {
		//    System.out.println("Another JFrame");
		//    glassPane.setVisible(false);
		target.getRootPane().setGlassPane(glassPane);
		glassPane.setVisible(true);
		Component c = target.getRootPane().getGlassPane();
		c.setCursor(isDropable ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
		if (isDropable)
		{
			//glassPane.setCursor(DragSource.DefaultMoveDrop);
			support.setShowDropLocation(true);
			dl.setDropable(true);
			target.setDropLocation(dl, null, true);
			return true;
		}
		else
		{
			//glassPane.setCursor(DragSource.DefaultMoveNoDrop);
			support.setShowDropLocation(false);
			dl.setDropable(false);
			target.setDropLocation(dl, null, false);
			return false;
		}
	}
//     private static boolean isWebStart() {
//         try{
//             javax.jnlp.ServiceManager.lookup("javax.jnlp.BasicService");
//             return true;
//         }catch(Exception ex) {
//             return false;
//         }
//     }

	private BufferedImage makeDragTabImage(DnDTabbedPane tabbedPane)
	{
		Rectangle rect = tabbedPane.getBoundsAt(tabbedPane.dragTabIndex);
		BufferedImage image = new BufferedImage(tabbedPane.getWidth(), tabbedPane.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		tabbedPane.paint(g);
		g.dispose();
		if (rect.x < 0)
		{
			rect.translate(-rect.x, 0);
		}
		if (rect.y < 0)
		{
			rect.translate(0, -rect.y);
		}
		if (rect.x + rect.width > image.getWidth())
		{
			rect.width = image.getWidth() - rect.x;
		}
		if (rect.y + rect.height > image.getHeight())
		{
			rect.height = image.getHeight() - rect.y;
		}
		return image.getSubimage(rect.x, rect.y, rect.width, rect.height);
	}
	private static GhostGlassPane glassPane;

	@Override
	public int getSourceActions(JComponent c)
	{
		System.out.println("getSourceActions");
		DnDTabbedPane src = (DnDTabbedPane) c;
		if (glassPane == null)
		{
			c.getRootPane().setGlassPane(glassPane = new GhostGlassPane(src));
		}
		if (src.dragTabIndex < 0)
		{
			return NONE;
		}
		glassPane.setImage(makeDragTabImage(src));
		//setDragImage(makeDragTabImage(src)); //java 1.7.0-ea-b84
		c.getRootPane().getGlassPane().setVisible(true);
		return MOVE;
	}

	@Override
	public boolean importData(TransferSupport support)
	{
		System.out.println("importData");
		if (!canImport(support))
		{
			return false;
		}

		DnDTabbedPane target = (DnDTabbedPane) support.getComponent();
		DnDTabbedPane.DropLocation dl = target.getDropLocation();
		try
		{
			DnDTabbedPane source = (DnDTabbedPane) support.getTransferable().getTransferData(localObjectFlavor);
			int index = dl.getIndex(); //boolean insert = dl.isInsert();
			if (target == source)
			{
				source.convertTab(source.dragTabIndex, index); //getTargetTabIndex(e.getLocation()));
			}
			else
			{
				source.exportTab(source.dragTabIndex, target, index);
			}
			return true;
		}
		catch (UnsupportedFlavorException ufe)
		{
			ufe.printStackTrace();
		}
		catch (java.io.IOException ioe)
		{
			ioe.printStackTrace();
		}
		return false;
	}

	@Override
	protected void exportDone(JComponent src, Transferable data, int action)
	{
		System.out.println("exportDone");
		//((DnDTabbedPane)src).setDropLocation(null, null, false);
		//src.getRootPane().getGlassPane().setVisible(false);
		glassPane.setVisible(false);
		glassPane = null;
		source = null;
	}


}

class GhostGlassPane extends JPanel
{
	private DnDTabbedPane tabbedPane;

	public GhostGlassPane(DnDTabbedPane tabbedPane)
	{
		this.tabbedPane = tabbedPane;
		//System.out.println("new GhostGlassPane");
		setOpaque(false);
		//http://bugs.sun.com/view_bug.do?bug_id=6700748
		//setCursor(null); //XXX
	}
	private BufferedImage draggingGhost = null;

	public void setImage(BufferedImage draggingGhost)
	{
		this.draggingGhost = draggingGhost;
	}

	public void setTargetTabbedPane(DnDTabbedPane tab)
	{
		tabbedPane = tab;
	}

	@Override
	public void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;
		DnDTabbedPane.DropLocation dl = tabbedPane.getDropLocation();
		Point p = getMousePosition(true); //dl.getDropPoint();
		if (draggingGhost != null && dl != null && p != null)
		{
			g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
			if (dl.isDropable())
			{
				tabbedPane.paintDropLine(g2);
			}
			//Point p = SwingUtilities.convertPoint(tabbedPane, dl.getDropPoint(), this);
			double xx = p.getX() - (draggingGhost.getWidth(this) / 2d);
			double yy = p.getY() - (draggingGhost.getHeight(this) / 2d);
			g2.drawImage(draggingGhost, (int) xx, (int) yy, this);
		}
	}
}
