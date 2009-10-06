import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Dimension;
import java.awt.event.*;
import java.beans.*;
import java.io.*;

public class SpaceFrame
{
	static JFrame frame;
	static Screen panel;
	static JMenuItem new_item;
	static JMenuItem load_item;
	static JMenuItem save_item;
	static JMenuItem exit_item;
	static JMenuItem about_item;
	static JMenuItem help_item;
	static JMenuItem saveas_item;
	static JMenuItem close_item;
	static menuHandler eHandler;
	static Object control;//GameControl
	static File cur_file;
	static JFileChooser fc;
	
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				JDialog.setDefaultLookAndFeelDecorated(true);
				JFrame.setDefaultLookAndFeelDecorated(true);
				
				frame=new JFrame("Space Wars");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				
				//create menu bar
				JMenuBar menu_bar=new JMenuBar();
				eHandler=new menuHandler();
				
				JMenu game_menu=new JMenu("Game");
				game_menu.setMnemonic(KeyEvent.VK_G);
				
				new_item=new JMenuItem("New");
				new_item.addActionListener(eHandler);
				new_item.setMnemonic(KeyEvent.VK_N);
				game_menu.add(new_item);
				
				load_item=new JMenuItem("Open");
				load_item.addActionListener(eHandler);
				load_item.setMnemonic(KeyEvent.VK_O);
				game_menu.add(load_item);
				
				close_item=new JMenuItem("Close");
				close_item.addActionListener(eHandler);
				close_item.setMnemonic(KeyEvent.VK_C);
				game_menu.add(close_item);
				
				save_item=new JMenuItem("Save");
				save_item.addActionListener(eHandler);
				save_item.setMnemonic(KeyEvent.VK_S);
				game_menu.add(save_item);
				
				saveas_item=new JMenuItem("Save As...");
				saveas_item.addActionListener(eHandler);
				saveas_item.setMnemonic(KeyEvent.VK_A);
				game_menu.add(saveas_item);
				
				game_menu.add(new JSeparator());
				
				exit_item=new JMenuItem("Exit");
				exit_item.addActionListener(eHandler);
				exit_item.setMnemonic(KeyEvent.VK_E);
				game_menu.add(exit_item);
				
				menu_bar.add(game_menu);
				
				
				JMenu help_menu=new JMenu("Help");
				help_menu.setMnemonic(KeyEvent.VK_H);
				
				about_item=new JMenuItem("About");
				about_item.addActionListener(eHandler);
				about_item.setMnemonic(KeyEvent.VK_A);
				help_menu.add(about_item);
				
				help_item=new JMenuItem("Help");
				help_item.addActionListener(eHandler);
				help_item.setMnemonic(KeyEvent.VK_H);
				help_menu.add(help_item);
				
				menu_bar.add(help_menu);
				
				frame.setJMenuBar(menu_bar);

				
				panel=new Screen(frame, (GameControl)control);
				panel.setPreferredSize(new Dimension(800,600));
				frame.add(panel);
				
				frame.pack();
				frame.setVisible(true);
				
				//set up file chooser for opening and saving
				fc=new JFileChooser();
				fc.setFileFilter(new FileNameExtensionFilter("XML files", "xml"));
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			}
		});
	}
	
	private static class menuHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent e)
		{
			if (e.getSource() == new_item)
			{
				control=new GameControl(frame);
				panel.control=(GameControl)control;
				
				panel.drawStatus();
				panel.pface.setVisible(true);
			}
			else if (e.getSource() == save_item)
				save(false);
			else if (e.getSource() ==saveas_item)
				save(true);
			else if(e.getSource()==load_item)
			{
				load();
				panel.control=(GameControl)control;
				
				panel.drawStatus();
				panel.pface.setVisible(true);
			}
			else if(e.getSource()==close_item)
				close();
			else if(e.getSource() == exit_item)
				frame.dispose();
			else if(e.getSource() == about_item)
				System.out.println("ABOUT");	
			else if(e.getSource() == help_item)
				System.out.println("HELP");
		}
	}
	
	private static void save(boolean save_as)
	{
		if(control instanceof GameControl)
		{
			boolean ask=(save_as||!(cur_file instanceof File));
			
			int returnVal=0;
			if(ask)
				returnVal = fc.showSaveDialog(frame);
			
			if (returnVal == JFileChooser.APPROVE_OPTION || !ask)
				{
				if(ask)
					cur_file = fc.getSelectedFile();
			
				try
				{
					XMLEncoder e = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(cur_file)));
	
					e.writeObject(control);
					e.close();
				}
				catch(FileNotFoundException f)
				{
					System.err.println("File not found exception in function save");
				}
			}
		}
	}

	private static void load()
	{
		int returnVal = fc.showOpenDialog(frame);
		
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			cur_file = fc.getSelectedFile();
			try
			{
				XMLDecoder d=new XMLDecoder(new BufferedInputStream(new FileInputStream(cur_file)));
				control = d.readObject();
				panel.control=(GameControl)control;
				d.close();
			}
			catch(FileNotFoundException e)
			{
				System.err.println("File not found exception in function load");
			}
		}
	}
	
	private static void close()
	{
		if(control instanceof GameControl)
		{
			if(JOptionPane.showConfirmDialog(frame, "Are you sure that you want to close the current game?\nAll unsaved data will be lost.", "Confirm Close", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
			{
				control=null;
				panel.control=null;
				cur_file=null;
			}
		}
	}
}