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
	public static abstract class ManufactureFuncs<ObjType, ObjMaker>
	{
		private ManufactureFuncs(){}
		public abstract boolean manufacture(ObjMaker maker, ObjType type, PlanetMoonCommandPanel p);
		public abstract Runnable getCallback(ObjMaker maker);
		public abstract void doneBuilding(PlanetMoonCommandPanel p);
	}
	
	public static ManufactureFuncs<ShipType, Shipyard> shipManufactureFuncs = new ManufactureFuncs<ShipType, Shipyard>()
		{
			@Override
			public boolean manufacture(Shipyard maker, ShipType type, PlanetMoonCommandPanel p)
			{
				boolean order_is_valid = maker.canBuild(type);
				if(order_is_valid)
				{
					GameInterface.GC.scheduleOrder(new ShipyardBuildShipOrder(maker, type, GameInterface.GC.updater.getTime()));
				}
				return order_is_valid;
			}
			
			@Override
			public void doneBuilding(PlanetMoonCommandPanel p)
			{
				p.return_to_queue = true;
				//p.build_ship.setEnabled(true);
				//p.displayQueue();
			}

			@Override
			public Runnable getCallback(Shipyard maker) {
				return new QueueUpdater(maker);
			}
		};
	public static ManufactureFuncs<FacilityType, OwnableSatellite<?>> facilityManufactureFuncs = new ManufactureFuncs<FacilityType,OwnableSatellite<?>>()
		{
			@Override
			public boolean manufacture(OwnableSatellite<?> maker, FacilityType type, PlanetMoonCommandPanel p)
			{
				p.need_to_reset = maker.canBuild(type);
				if(p.need_to_reset)
				{
					GameInterface.GC.scheduleOrder(new FacilityBuildOrder(maker, type, GameInterface.GC.updater.getTime()));
				}
				return p.need_to_reset;
			}
			
			@Override
			public void doneBuilding(PlanetMoonCommandPanel p)
			{
				//DO NOTHING!  callback should be enough
				
				//p.setSat(p.the_sat);
				/*p.facilities_panel.removeAll();
				p.displayAllFacilities();
				p.facilities_panel.repaint();*/
			}
			
			@Override
			public Runnable getCallback(OwnableSatellite<?> maker)
			{
				return new Callback(maker);
			}
			
			class Callback implements Runnable
			{
				OwnableSatellite<?> sat;
				
				public Callback(OwnableSatellite<?> s)
				{
					sat = s;
				}
				
				public void run()
				{
					if(GameInterface.GC.GI != null)
					{
						PlanetMoonCommandPanel p = GameInterface.GC.GI.SatellitePanel;
						if(p != null && sat == p.the_sat
								&& (p.state == PlanetMoonCommandPanel.PANEL_STATE.FACILITIES_DISPLAYED 
										|| p.state == PlanetMoonCommandPanel.PANEL_STATE.FACILITY_CHOICES_DISPLAYED)  //TODO: if we ever have a queue of facilities to build, this won't work right
								)
						{
							p.setSat(sat); //TODO: this seems like a bit overkill... but I can't get the facilities to reappear
							/*p.facilities_panel.removeAll();
							p.displayAllFacilities();
							p.facilities_panel.repaint();*/
						}
					}
				}
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