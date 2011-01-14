import java.awt.event.*;
import java.awt.Color;
import javax.swing.JPanel;

public class ObjBuilder<ObjType, ObjMaker> implements MouseListener
{
	ObjMaker maker;
	ObjType type;
	ObjBuilder.ManufactureFuncs<ObjType,ObjMaker> actions;
	boolean enable_shift;
	
	PlanetMoonCommandPanel panel;
	JPanel type_panel;
	boolean mouse_in;
	boolean was_clicked_on;
	
	
	public ObjBuilder(ObjMaker s, ObjType t, ObjBuilder.ManufactureFuncs<ObjType,ObjMaker> acts, JPanel typepanel, boolean shift_en, PlanetMoonCommandPanel p)
	{
		maker=s;
		type=t;
		actions = acts;
		enable_shift = shift_en;
		
		panel=p;
		type_panel=typepanel;
		
		type_panel.setOpaque(false);
		type_panel.setBackground(new Color(150,150,255));
	}
	
	/**Client code should not directly instantiate a version of this inner class,
	 * but should rather use one of the static variables ShipManufactureFuncs or
	 * FacilityManufactureFuncs*/
	private static abstract class ManufactureFuncs<ObjType, ObjMaker>
	{
		public abstract boolean manufacture(ObjMaker maker, ObjType type, PlanetMoonCommandPanel p);
		public abstract void doneBuilding(PlanetMoonCommandPanel p);
	}
	
	public static ManufactureFuncs<ShipType, Shipyard> ShipManufactureFuncs = new ManufactureFuncs<ShipType, Shipyard>()
		{
			public boolean manufacture(Shipyard maker, ShipType type, PlanetMoonCommandPanel p)
			{
				return maker.addToQueue(new Ship(type), GameInterface.GC.updater.getTime(), true);
			}
			public void doneBuilding(PlanetMoonCommandPanel p)
			{
				p.build_ship.setEnabled(true);
				p.displayQueue();
			}
		};
	public static ManufactureFuncs<FacilityType, OwnableSatellite<?>> FacilityManufactureFuncs = new ManufactureFuncs<FacilityType,OwnableSatellite<?>>()
		{
			public boolean manufacture(OwnableSatellite<?> maker, FacilityType type, PlanetMoonCommandPanel p)
			{
				p.need_to_reset = maker.scheduleConstruction(type, GameInterface.GC.updater.getTime(), true);
				return p.need_to_reset;
			}
			
			public void doneBuilding(PlanetMoonCommandPanel p)
			{
				p.setSat(p.the_sat); //TODO: this seems like a bit overkill... but I can't get the facilities to reappear
				/*p.facilities_panel.removeAll();
				p.displayAllFacilities();
				p.facilities_panel.repaint();*/
			}
		};
	
	public void mouseReleased(MouseEvent e)
	{
		if(mouse_in)
		{
			if(actions.manufacture(maker,type,panel))
			{
				if(!(e.isShiftDown() && enable_shift))
				{
					actions.doneBuilding(panel);
				}
			}
			else
				SoundManager.playSound("sound/doot doot.wav");
			type_panel.setOpaque(false);
			type_panel.repaint();
		}
		was_clicked_on=false;
	}
	
	public void mouseEntered(MouseEvent e)
	{
		if(was_clicked_on)
		{
			type_panel.setOpaque(true);
			type_panel.repaint();
			mouse_in=true;
		}
	}
	
	public void mouseExited(MouseEvent e)
	{
		type_panel.setOpaque(false);
		type_panel.repaint();
		mouse_in=false;
	}
	
	public void mousePressed(MouseEvent e)
	{
		type_panel.setOpaque(true);

		type_panel.repaint();
		mouse_in=true;
		was_clicked_on=true;
	}
	public void mouseClicked(MouseEvent e){}
}