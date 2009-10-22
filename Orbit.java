import java.util.*;

public class Orbit
{
	static double PERIOD_CONSTANT=1000; //equiv to 2pi/sqrt(G), where G is the gravity constant
	int init_x;
	int init_y;
	
	int focus2_x;
	int focus2_y;
	
	int cur_x;
	int cur_y;
	double period;
	double time_offset;
	int direction; //1=clockwise, -1=counterclockwise
	
	double a;
	double b;
	double c;
	
	Positioning boss;
	Satellite obj;
	
	public Orbit(Satellite theobj, Positioning boss_obj, int focus2_x, int focus2_y, int init_x, int init_y, int dir)
	{
		boss=boss_obj;
		obj=theobj;
		
		direction=dir;
		this.init_x=init_x-boss.absoluteCurX();
		this.init_y=init_y-boss.absoluteCurY();
		this.focus2_x=focus2_x-boss.absoluteCurX();
		this.focus2_y=focus2_y-boss.absoluteCurY();
		
		cur_x=init_x-boss.absoluteCurX();
		cur_y=init_y-boss.absoluteCurY();
		
		calculateOrbit();
	}
	
	public void calculateOrbit()
	{
		a = (Math.hypot((double)init_x, (double)init_y)+Math.hypot((double)focus2_x-init_x, (double)focus2_y-init_y))/2;
		HashSet<Double> mass_set=boss.getMassSet();
		mass_set.add(obj.getMass());
		double mass_prod=1;
		double inv_sum=0;
		for(double mass : mass_set)
		{
			mass_prod *= mass;
			inv_sum += 1/mass;
		}
		period=PERIOD_CONSTANT*Math.sqrt(Math.pow(2*a,3.0d)/(inv_sum*mass_prod));
		calcTimeOffset();
	}
	
	public void calcTimeOffset()
	{
		
		c=Math.hypot((double)focus2_x,(double)focus2_y)/2;
		b=Math.sqrt(Math.pow(a,2.0d)-Math.pow(c,2.0d));
		
		double rot_angle;
		if(focus2_x != 0)
			rot_angle=-Math.atan(focus2_y/focus2_x);
		else if(focus2_y>0)
			rot_angle = -Math.PI/2;
		else if(focus2_y<0)
			rot_angle = Math.PI/2;
		else
			rot_angle=0; //doesn't matter, perfect circle
		
		double shift_x = (double)(init_x-focus2_x/2);
		double shift_y = (double)(init_y-focus2_y/2);
		
		double rot_x = shift_x*Math.cos(rot_angle) + shift_y*Math.sin(rot_angle);
		double rot_y = -shift_x*Math.sin(rot_angle) + shift_y*Math.cos(rot_angle);
		
		double theta = Math.acos(rot_x/a);
		if(rot_y < 0)
			theta = 2*Math.PI - theta;
		time_offset = period/(2*Math.PI)*(theta-c/a*Math.sin(theta));
	}
	
	public int absoluteCurX(){return cur_x + boss.absoluteCurX();}
	public int absoluteCurY(){return cur_y + boss.absoluteCurY();}
	
	public int absoluteInitX(){return init_x + boss.absoluteInitX();}
	public int absoluteInitY(){return init_y + boss.absoluteInitY();}
	
	public void move(double time)
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
		while(Math.abs(theta2-c/a*Math.sin(theta2) - 2*Math.PI*frac_time)>.000000001 || Math.abs(theta1-theta2)>.000000001);
		
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
		if(focus2_x != 0)
			rot_angle=-Math.atan(focus2_y/focus2_x);
		else if(focus2_y>0)
			rot_angle = Math.PI/2;
		else if(focus2_y<0)
			rot_angle = -Math.PI/2;
		else
			rot_angle=0; //doesn't matter, perfect circle
		
		double needs_shift_x = needs_rot_x*Math.cos(rot_angle) + needs_rot_y*Math.sin(rot_angle);
		double needs_shift_y = -needs_rot_x*Math.sin(rot_angle) + needs_rot_y*Math.cos(rot_angle);
		
		cur_x=(int)(needs_shift_x+focus2_x/2); //shift by focus2x/2?
		cur_y=(int)(needs_shift_y+focus2_y/2); //shift by focus2y/2?
	}
	
	//methods required for save/load
	public Orbit(){}
	public int getInit_x(){return init_x;}
	public void setInit_x(int x){init_x=x;}
	public int getInit_y(){return init_y;}
	public void setInit_y(int y){init_y=y;}
	public int getFocus2_x(){return focus2_x;}
	public void setFocus2_x(int x){focus2_x=x;}
	public int getFocus2_y(){return focus2_y;}
	public void setFocus2_y(int y){focus2_y=y;}
	public int getCur_x(){return cur_x;}
	public void setCur_x(int x){cur_x=x;}
	public int getCur_y(){return cur_y;}
	public void setCur_y(int y){cur_y=y;}
	public double getPeriod(){return period;}
	public void setPeriod(double d){period=d;}
	public int getDirection(){return direction;}
	public void setDirection(int d){direction=d;}
}