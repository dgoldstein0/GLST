package galactic_strategy.game_objects;

public class ShipId extends Flyer.FlyerId<ShipId> implements Comparable<ShipId>
{
	Shipyard manufacturer;
	int queue_id;
	
	public ShipId(int q_id, Shipyard manu)
	{
		manufacturer = manu;
		queue_id = q_id;
	}
	
	public ShipId(){};
	
	@Override
	public int hashCode()
	{
		if(manufacturer != null)
			return manufacturer.hashCode()*211 + queue_id;
		else
			return 0;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o instanceof ShipId)
		{
			ShipId s = (ShipId)o;
			return (s.manufacturer == manufacturer) && (s.queue_id == queue_id);
		}
		else //this will catch the case where o is null
			return false;
	}

	@Override
	public int compareTo(ShipId o) {
		if (manufacturer != null && o.manufacturer != null) {
			int manufacturer_comp = manufacturer.compareTo(o.manufacturer);
			if (manufacturer_comp != 0)
				return manufacturer_comp;
		}
		// the XmlEncoder from java beans will end up calling this with
		// partially-instantiated objects by recursing to instantiate a
		// manufacturer (shipyard) and eventually ending up back at the
		// same object in the object graph; this makes us at least try
		// to do something sane in these cases.
		else if (manufacturer == null && o.manufacturer != null) {
			return -1;
		}
		else if (o.manufacturer == null && manufacturer != null) {
			return 1;
		}
		
		if (queue_id < o.queue_id)
			return -1;
		else if (queue_id == o.queue_id)
			return 0;
		else
			return 1;
	}

	public void setManufacturer(Shipyard m) {this.manufacturer = m;}
	public Shipyard getManufacturer() {return manufacturer;}
	public void setQueue_id(int queue_id) {this.queue_id = queue_id;}
	public int getQueue_id() {return queue_id;}
}
