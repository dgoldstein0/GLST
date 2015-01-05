package galactic_strategy.game_objects;

public class FlyerCapabilities implements FlyingThing.Capabilities {
	private ShipType type;
	private Flyer<?, ?, ?> flyer;
	
	public FlyerCapabilities(Flyer<?, ?, ?> f, ShipType t) {
		type = t;
		flyer = f;
	}
	
	@Override public double getMaxSpeed() {
		return type.max_speed;
	}
	
	@Override public double getMaxAngularVelocity() {
		return type.max_angular_vel;
	}
	
	@Override public double getAccel() {
		if (flyer.isInWarpTransition())
			return type.warp_accel;
		else
			return type.accel_rate;
	}
	
	@Override public boolean enforceSpeedCap() {
		return !flyer.isInWarpTransition();
	}
	
	// for the xml encoder
	public FlyerCapabilities() {}
	public ShipType getType(){return type;}
	public void setType(ShipType t){type = t;}
	public Flyer<?,?,?> getFlyer(){return flyer;}
	public void setFlyer(Flyer<?,?,?> f){flyer = f;}
}
