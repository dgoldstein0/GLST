import java.util.*;

public class Orbit
{
	static double PERIOD_CONSTANT=GalacticStrategyConstants.PERIOD_CONSTANT;

	static int CLOCKWISE = 1;
	static int COUNTERCLOCKWISE=-1;

	//equiv to 2pi/sqrt(G), where G is the gravity constant
	double init_x;
	double init_y;
	
	Focus focus2;
	
	double cur_x;
	double cur_y;
	double period;
	double time_offset;
	int direction; //1=clockwise, -1=counterclockwise - use constants CLOCKWISE and COUNTERCLOCKWISE
	
	double a;
	double b;
	double c;
	
	Positioning boss;
	Satellite obj;
	
	public Orbit(Satellite theobj, Positioning boss_obj, double focus2_x, double focus2_y, double init_x, double init_y, int dir)
	{
		boss=boss_obj;
		obj=theobj;
		
		direction=dir;
		this.init_x=init_x-boss.absoluteCurX();
		this.init_y=init_y-boss.absoluteCurY();
		focus2 = new Focus(focus2_x-boss.absoluteCurX(),focus2_y=focus2_y-boss.absoluteCurY(),this);
		
		cur_x=init_x-boss.absoluteCurX();
		cur_y=init_y-boss.absoluteCurY();
		
		calculateOrbit();
	}
	
	public synchronized void calculateOrbit()
	{
		a = (Math.hypot(init_x, init_y)+Math.hypot(focus2.getX()-init_x, focus2.getY()-init_y))/2.0d;
		
		//Calculate period.  This depends on mass.

		//*****reduced mass, which I tried before, neither seems to work here nor make sense (or is implemented wrong).  Thus this code has been commented out and replaced
		/*HashSet<Double> mass_set=boss.getMassSet();
		mass_set.add(obj.getMass());
		
		double mass_prod=1;
		double inv_sum=0;
		for(double mass : mass_set)
		{
			mass_prod *= mass;
			inv_sum += 1/mass;
		}
		period=PERIOD_CONSTANT*Math.sqrt(Math.pow(2*a,3.0d)/(inv_sum*mass_prod));*/
		
		double mass_sum=boss.massSum() + obj.getMass();		
		period=PERIOD_CONSTANT*Math.sqrt(Math.pow(2*a, 3.0d))/mass_sum;
		
		//System.out.println(Double.toString(mass_sum)); //this was used for debugging
		
		calcTimeOffset();
	}
	
	public void calcTimeOffset()
	{
		c=Math.hypot(focus2.getX(),focus2.getY())/2.0d;
		b=Math.sqrt(a*a-c*c);
		
		double rot_angle;
		if(focus2.getX() != 0)
			rot_angle=Math.atan(focus2.getY()/focus2.getX()); //when this was negated, it was the source of the incorrect start position bug.  The inverted y measurements results in an implicit negative sign, so the added negative screwed stuff up
		else if(focus2.getY()>0)
			rot_angle = -Math.PI/2.0d;
		else if(focus2.getY()<0)
			rot_angle = Math.PI/2.0d;
		else
			rot_angle=0; //doesn't matter, perfect circle
		
		double shift_x = init_x-focus2.getX()/2.0d;
		double shift_y = init_y-focus2.getY()/2.0d;
		
		double rot_x = shift_x*Math.cos(rot_angle) + shift_y*Math.sin(rot_angle);
		double rot_y = -shift_x*Math.sin(rot_angle) + shift_y*Math.cos(rot_angle);
		
		double theta = Math.acos(rot_x/a);
		if(rot_y < 0)
			theta = 2*Math.PI - theta;
		time_offset = period/(2*Math.PI)*(theta-c/a*Math.sin(theta));
	}
	
	public double absoluteCurX(){return cur_x + boss.absoluteCurX();}
	public double absoluteCurY(){return cur_y + boss.absoluteCurY();}
	
	public double absoluteInitX(){return init_x + boss.absoluteInitX();}
	public double absoluteInitY(){return init_y + boss.absoluteInitY();}
	
	public synchronized void move(double time)
	{		
		double frac_time = (time_offset + ((double)direction)*time)/period; //ADD IN TIME!
		frac_time=frac_time-Math.floor(frac_time);
		
		double theta1;
		double theta2;
		
		theta2=frac_time*2*Math.PI;
		
		//newton's method... should converge- derivative is 2 at pi
		do
		{
			theta1=theta2;
			double d_at_theta1=1-c/a*Math.cos(theta1);
			theta2=theta1-(theta1-c/a*Math.sin(theta1) - 2*Math.PI*frac_time)/d_at_theta1;
		}
		while(Math.abs(theta2-c/a*Math.sin(theta2) - 2*Math.PI*frac_time)>.00000000001 || Math.abs(theta1-theta2)>.00000000001);
		
		double needs_rot_x = a*Math.cos(theta2);
		
		//redo this algorithm to simplify calculations - perhaps this original formula didn't even work...
		/*double phi;
		if(needs_rot_x != 0)
		{
			phi=Math.atan(b/a*Math.tan(theta2));
			if(needs_rot_x < 0)
				phi += Math.PI;
		}
		else
			phi=theta2;
		
		double needs_rot_y = Math.sqrt((1-b*b/(a*a))*Math.pow(needs_rot_x,2.0d)+b*b)*Math.sin(phi);*/

		double needs_rot_y=0; //this will be overwritten
		if(needs_rot_x != 0)		
			needs_rot_y = needs_rot_x*b/a*Math.tan(theta2);
		else if(theta2 == Math.PI/2)
			needs_rot_y = b;
		else if(theta2 == 3*Math.PI/2)
			needs_rot_y=-b;

		
		double rot_angle;
		if(focus2.getX() != 0)
			rot_angle=-Math.atan(focus2.getY()/focus2.getX());
		else if(focus2.getY()>0)
			rot_angle = Math.PI/2;
		else if(focus2.getY()<0)
			rot_angle = -Math.PI/2;
		else
			rot_angle=0; //doesn't matter, perfect circle
		
		double needs_shift_x = needs_rot_x*Math.cos(rot_angle) + needs_rot_y*Math.sin(rot_angle);
		double needs_shift_y = -needs_rot_x*Math.sin(rot_angle) + needs_rot_y*Math.cos(rot_angle);
		
		cur_x=(int)(needs_shift_x+focus2.getX()/2); //shift by focus2x/2?
		cur_y=(int)(needs_shift_y+focus2.getY()/2); //shift by focus2y/2?
	}
	
	//methods required for save/load
	public Orbit(){}
	public Positioning getBoss(){return boss;}
	public void setBoss(Positioning p){boss=p;}
	public Satellite getObj(){return obj;}
	public void setObj(Satellite o){obj=o;}
	public double getInit_x(){return init_x;}
	public void setInit_x(double x){init_x=x;}
	public double getInit_y(){return init_y;}
	public void setInit_y(double y){init_y=y;}
	public Focus getFocus2(){return focus2;}
	public void setFocus2(Focus x){focus2=x;}
	public double getCur_x(){return cur_x;}
	public void setCur_x(double x){cur_x=x;}
	public double getCur_y(){return cur_y;}
	public void setCur_y(double y){cur_y=y;}
	public double getPeriod(){return period;}
	public void setPeriod(double d){period=d;}
	public int getDirection(){return direction;}
	public void setDirection(int d){direction=d;}
	public double getA(){return a;}
	public void setA(double x){a=x;}
	public double getB(){return b;}
	public void setB(double y){b=y;}
	public double getC(){return c;}
	public void setC(double z){c=z;}
}