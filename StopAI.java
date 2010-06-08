public class StopAI extends FlyerAI
{
	public StopAI(){}
	
	public double calcDesiredDirection(){return 0.0;}
	public double calcDesiredSpeed(double dir_chng){return 0.0;}
	public int directionType(){return FlyerAI.REL_DIRECTION;}
}