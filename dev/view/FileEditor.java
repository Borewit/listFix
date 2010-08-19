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

package listfix.view;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.util.Locale;

import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.swing.*;
import javax.swing.border.*;

public class FileEditor extends JFrame
{
	private Clipboard clipboard = null;
	private File openFile = null;
	private JLabel fileStatus = null;
	private JLabel insertStatus = null;
	private JLabel capslockStatus = null;
	private JMenuBar menuBar = null;
	private JMenuBar statusBar = null;
	private JPanel mainPanel = null;
	private JPopupMenu popup = null;
	private JScrollPane scrollPane = null;
	private JTextArea textArea = null;
	private Toolkit toolkit = null;

	public FileEditor(String title)
	{
		super(title);
		initializeComponents();
	}

	public FileEditor(String filename, String title)
	{
		super(title);
		File file = new File(filename);
		initializeComponents();
		System.out.println(filename);
		openDocument(file);
	}

	private void initializeComponents()
	{
		textArea = new JTextArea();
		mainPanel = new JPanel(new BorderLayout());
		this.getContentPane().add(mainPanel);
		toolkit = Toolkit.getDefaultToolkit();
		clipboard = toolkit.getSystemClipboard();

		statusBar = new JMenuBar();
		statusBar.setLayout(new BorderLayout());
		statusBar.setBorder(new EtchedBorder());
		statusBar.setBorderPainted(true);

		fileStatus = new JLabel(" ");
		fileStatus.setBorder(new EtchedBorder());

		insertStatus = new JLabel(" ");
		insertStatus.setPreferredSize((new Dimension(26, 20)));
		insertStatus.setBorder(new EtchedBorder());
		capslockStatus = new JLabel(" ");
		capslockStatus.setPreferredSize(new Dimension(36, 20));
		capslockStatus.setBorder(new EtchedBorder());

		System.out.println(KeyEvent.VK_CAPS_LOCK);
		boolean b = toolkit.getLockingKeyState(20);
		if (b)
		{
			capslockStatus.setText("CAPS");
		}

		JPanel status2 = new JPanel(new BorderLayout());
		JPanel status3 = new JPanel(new BorderLayout());
		status3.add(insertStatus, BorderLayout.EAST);
		status3.add(status2, BorderLayout.CENTER);
		status2.add(capslockStatus, BorderLayout.EAST);
		status2.add(fileStatus, BorderLayout.CENTER);
		statusBar.add(status3);

		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic('F');

		final JMenuItem novo = new JMenuItem("New");
		novo.setMnemonic('N');
		novo.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				newFile();

			}
		});

		JMenuItem openMenuItem = new JMenuItem("Open");
		openMenuItem.setMnemonic('O');
		openMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				openFile();

			}
		});


		JMenuItem saveMenuItem = new JMenuItem("Save");
		saveMenuItem.setMnemonic('S');
		saveMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				save();

			}
		});


		JMenuItem saveAsMenuItem = new JMenuItem("Save As");
		saveAsMenuItem.setMnemonic('v');
		saveAsMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				saveAs();

			}
		});

		JMenuItem printMenuItem = new JMenuItem("Print");
		printMenuItem.setMnemonic('P');
		printMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				if (openFile != null)
				{
					print();
				}

			}
		});

		JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.setMnemonic('x');
		exitMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				exit();
			}
		});

		fileMenu.add(novo);
		fileMenu.add(openMenuItem);
		fileMenu.add(new JSeparator());
		fileMenu.add(saveMenuItem);
		fileMenu.add(saveAsMenuItem);
		fileMenu.add(new JSeparator());
		fileMenu.add(exitMenuItem);

		JMenuItem cutMenuItem = new JMenuItem("Cut");
		cutMenuItem.setMnemonic('u');
		cutMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				cut();
			}
		});

		JMenuItem copyMenuItem = new JMenuItem("Copy");
		copyMenuItem.setMnemonic('C');
		copyMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				copy();

			}
		});

		JMenuItem pasteMenuItem = new JMenuItem("Paste");
		pasteMenuItem.setMnemonic('P');
		pasteMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				paste();

			}
		});


		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic('E');
		editMenu.add(cutMenuItem);
		editMenu.add(copyMenuItem);
		editMenu.add(pasteMenuItem);

		JMenuItem cutPopupMenuItem = new JMenuItem("Cut");
		cutPopupMenuItem.setMnemonic('u');
		cutPopupMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				cut();

			}
		});

		JMenuItem copyPopupMenuItem = new JMenuItem("Copy");
		copyPopupMenuItem.setMnemonic('C');
		copyPopupMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				copy();

			}
		});

		JMenuItem pastePopupMenuItem = new JMenuItem("Paste");
		pastePopupMenuItem.setMnemonic('P');
		pastePopupMenuItem.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				paste();

			}
		});


		popup = new JPopupMenu();
		popup.add(cutPopupMenuItem);
		popup.add(copyPopupMenuItem);
		popup.add(pastePopupMenuItem);

		menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(editMenu);

		scrollPane = new JScrollPane(textArea);

		JPanel superior = new JPanel(new BorderLayout(0, 0));
		superior.add(menuBar, BorderLayout.NORTH);

		mainPanel.add(BorderLayout.NORTH, superior);
		mainPanel.add(BorderLayout.CENTER, scrollPane);
		mainPanel.add(BorderLayout.SOUTH, statusBar);

		textArea.add(popup);

		textArea.addMouseListener(new MouseListener()
		{
			public void mouseClicked(MouseEvent e)
			{
			}

			public void mouseEntered(MouseEvent e)
			{
			}

			public void mouseExited(MouseEvent e)
			{
			}

			public void mousePressed(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					popup.show(e.getComponent(),
						e.getX(), e.getY());
				}
			}

			public void mouseReleased(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					popup.show(e.getComponent(),
						e.getX(), e.getY());
				}
			}
		});

		textArea.addKeyListener(new KeyListener()
		{
			public void keyPressed(KeyEvent e)
			{
				switch (e.getKeyCode())
				{
					case 155:
					{
						if (insertStatus.getText().equals(" "))
						{
							insertStatus.setText("INS");
						}
						else
						{
							insertStatus.setText(" ");
						}
						break;
					}
					case 20:
					{
						if (capslockStatus.getText().equals(" "))
						{
							capslockStatus.setText("CAPS");
						}
						else
						{
							capslockStatus.setText(" ");
						}
						break;
					}
				}
			}

			public void keyReleased(KeyEvent e)
			{
			}

			public void keyTyped(KeyEvent e)
			{
			}
		});

		textArea.setLocale(Locale.getDefault());
		textArea.setEditable(true);
		textArea.setFont(new Font("Dialog", Font.PLAIN, 13));
		this.setSize(new Dimension(500, 500));
		this.setVisible(true);
	}

	private void newFile()
	{
		openFile = null;
		textArea.setText("");
		fileStatus.setText(" ");
	}

	private void openFile()
	{
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setApproveButtonText("Open");
		fileChooser.setApproveButtonToolTipText("Open File");
		fileChooser.setDialogTitle("Open...");
		int returnVal = fileChooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			newFile();
			openFile = fileChooser.getSelectedFile();
			openDocument(openFile);
		}
	}

	private void openDocument(File file)
	{
		if (file.exists())
		{
			openFile = file;
			try
			{
				BufferedReader reader;
				System.out.println(openFile.getCanonicalPath());
				reader = new BufferedReader(new FileReader(openFile));
				String line = null;
				while ((line = reader.readLine()) != null)
				{
					textArea.append(line + '\n');
				}
				fileStatus.setText("Open file: " + openFile.getCanonicalPath());
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			fileStatus.setText(" ");
		}
	}

	private void save()
	{
		if (openFile == null)
		{
			saveAs();
		}
		else
		{
			try
			{
				BufferedWriter bw = new BufferedWriter(new FileWriter(openFile));
				bw.write(textArea.getText());
				bw.close();
			}
			catch (IOException io)
			{
				io.printStackTrace();
			}
		}
	}

	private void saveAs()
	{
		JFileChooser chooser = new JFileChooser();
		chooser.setApproveButtonText("Save");
		chooser.setDialogTitle("Save As...");
		chooser.setMultiSelectionEnabled(false);
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			try
			{
				openFile = chooser.getSelectedFile();
				BufferedWriter writer = new BufferedWriter(new FileWriter(openFile));
				writer.write(textArea.getText());
				writer.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void print()
	{
		FileInputStream txtStream = null;
		try
		{
			txtStream = new FileInputStream(openFile);
		}
		catch (FileNotFoundException ffne)
		{
			ffne.printStackTrace();
		}
		if (txtStream == null)
		{
			return;
		}

		DocFlavor flavor = DocFlavor.INPUT_STREAM.TEXT_PLAIN_HOST;
		Doc doc = new SimpleDoc(txtStream, flavor, null);
		PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();

		attributes.add(new Copies(1));
		attributes.add(MediaSize.getMediaSizeForName(MediaSizeName.ISO_A4));
		attributes.add(Sides.ONE_SIDED);

		PrintService[] services = PrintServiceLookup.lookupPrintServices(flavor, attributes);

		if (services.length > 0)
		{
			DocPrintJob job = services[0].createPrintJob();
			try
			{
				job.print(doc, attributes);
			}
			catch (PrintException pe)
			{
				System.out.println("Could not print this document...");
				pe.printStackTrace();
			}
		}

	}

	private void cut()
	{
		String toCut = textArea.getSelectedText();
		if (toCut != null)
		{
			textArea.replaceSelection(null);
			StringSelection auxiliar = new StringSelection(toCut);
			clipboard.setContents(auxiliar, new StringSelection("self"));
		}
	}

	private void copy()
	{
		String selected = textArea.getSelectedText();
		if (selected != null)
		{
			StringSelection helper = new StringSelection(selected);
			clipboard.setContents(helper, new StringSelection("self"));
		}
	}

	private void paste()
	{
		String toPaste = null;
		Transferable helper = null;
		helper = clipboard.getContents(null);
		if (helper.isDataFlavorSupported(DataFlavor.stringFlavor))
		{
			try
			{
				toPaste = (String) helper.getTransferData(DataFlavor.stringFlavor);
			}
			catch (UnsupportedFlavorException e)
			{
				System.out.println("Content type not supprted...");
				e.printStackTrace();
				toPaste = null;
			}
			catch (IOException e)
			{
				System.out.println("I/O error");
				e.printStackTrace();
			}
		}
		if (toPaste != null)
		{
			textArea.replaceSelection(toPaste);
		}
	}

	private void exit()
	{
		this.setVisible(false);
		this.dispose();
	}

	public static void main(String[] args)
	{
		if (args.length > 0)
		{
			for (int i = 0; i < args.length; i++)
			{
				System.out.println(args[i]);
				new FileEditor(args[i]);
			}
		}
		else
		{
			new FileEditor("Main method testing...");
		}
	}
}
