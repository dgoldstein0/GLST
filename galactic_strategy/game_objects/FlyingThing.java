package galactic_strategy.game_objects;

import galactic_strategy.Constants;
import galactic_strategy.sync_engine.DataSaverControl;
import galactic_strategy.sync_engine.Saveable;

/**
 * @author David
 *
 * This class is an abstraction of the logic for things that move around in
 * the game. It might not represent a physical object - it could represent,
 * instead, an arrangement of other actual game objects (e.g. a Formation).
 */
public strictfp class FlyingThing implements Saveable<FlyingThing>{
	
	/**
	 * @author David
	 *
	 * This class describes the capabilities of the flying object.  In many
	 * cases it's functions will just return constants, but this allows some
	 * degree of more flexible behavior.
	 */
	public abstract interface Capabilities {
		public abstract boolean enforceSpeedCap();
		public abstract double getMaxSpeed();
		public abstract double getMaxAngularVelocity();
		public abstract double getAccel();
	}

	private double pos_x; //pos_x and pos_y indicate where in the system the ship is located
	private double pos_y;
	private double direction;
	private double speed;

	FlyerAI current_flying_AI;
	AbstractDestination<?> destination;
	private FlyingThing.Capabilities capabilities;
	DataSaverControl<FlyingThing> data_control;

	FlyingThing(FlyingThing.Capabilities cap) {
		destination = null;
		capabilities = cap;
		data_control = new DataSaverControl<FlyingThing>(this);
	}
	
	//moves the ship one time_granularity.  this is a separate function so that all ships updates can be stepped through 1 by 1.
	protected void moveIncrement(long t)
	{
		//change position
		pos_x += speed*Constants.TIME_GRANULARITY*Math.cos(direction);
		pos_y += speed*Constants.TIME_GRANULARITY*Math.sin(direction);
		
		double desired_direction_chng;

		switch(current_flying_AI.directionType())
		{
			case FlyerAI.ABS_DIRECTION:
				desired_direction_chng = current_flying_AI.calcDesiredDirection(t) - direction;
				if(desired_direction_chng > Math.PI)
					desired_direction_chng -= 2.0*Math.PI;
				else if(desired_direction_chng < -Math.PI)
					desired_direction_chng += 2.0*Math.PI;
				break;
			case FlyerAI.REL_DIRECTION:
				desired_direction_chng = current_flying_AI.calcDesiredDirection(t);
				break;
			default:
				System.out.println("directionType not supported by Flyer.moveIncrement");
				return; //to avoid that desired_direction_chng might not be initialized.
				//break;
		}
		
		double desired_speed = current_flying_AI.calcDesiredSpeed(t, desired_direction_chng);
		
		double accel = capabilities.getAccel(); // TODO
		
		//change speed
		if(desired_speed < speed)
			speed = Math.max(Math.max(speed - accel*Constants.TIME_GRANULARITY, desired_speed), 0.0d);
		else
		{
			if(capabilities.enforceSpeedCap()) //false allows ships to exceed their speed limitations
				speed = Math.min(Math.min(speed + accel*Constants.TIME_GRANULARITY, desired_speed), capabilities.getMaxSpeed());
			else
				speed = Math.min(speed + accel*Constants.TIME_GRANULARITY, desired_speed);
		}
		
		// *** change direction ***
		// finds the absolute value of the amount the direction changes
		double actual_chng = Math.min(
				Math.abs(desired_direction_chng),
				Math.abs(capabilities.getMaxAngularVelocity()*Constants.TIME_GRANULARITY)
		);
		if(desired_direction_chng > 0)
			direction += actual_chng;
		else
			direction -= actual_chng;
		
		// keep direction between +/- pi
		if(direction > Math.PI)
			direction -= 2*Math.PI;
		else if(direction < -Math.PI)
			direction += 2*Math.PI;
	}
	
	public boolean reachedDest(){
		boolean isClose = Constants.CloseEnoughDistance > findSqDistance(destination);
		return isClose;
	}
	
	public double findSqDistance(AbstractDestination<?> target)
	{
		double deltaX=target.getXCoord()-pos_x;
		double deltaY=target.getYCoord()-pos_y;
		return MathFormula.SumofSquares(deltaX, deltaY);
	}

	public void setInitialPositionAndVelocity(double x, double y,
			double speed, double direction) {
		pos_x = x;
		pos_y = y;
		this.speed = speed;
		this.direction = direction;
	}
	
	public FlyingThing() {data_control = new DataSaverControl<FlyingThing>(this);}
	public double getPos_x(){return pos_x;}
	public double getPos_y(){return pos_y;}
	public void setPos_x(double x){pos_x=x;}
	public void setPos_y(double y){pos_y=y;}
	public double getSpeed(){return speed;}
	public double getDirection(){return direction;}
	public void setSpeed(double s){speed=s;}
	public void setDirection(double d){direction=d;}
	public FlyerAI getCurrent_flying_AI(){return current_flying_AI;}
	public void setCurrent_flying_AI(FlyerAI ai){current_flying_AI = ai;}
	public AbstractDestination<?> getDestination() {return destination;}
	public void setDestination(AbstractDestination<?> destination) {this.destination = destination;}
	public FlyingThing.Capabilities getCapabilities() {return capabilities;}
	public void setCapabilities(FlyingThing.Capabilities c) {capabilities = c;}

	@Override
	public DataSaverControl<FlyingThing> getDataControl() {
		return data_control;
	}
}
