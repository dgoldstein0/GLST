public strictfp class Orbit implements Saveable<Orbit>
{
	//equiv to 2pi/sqrt(G), where G is the gravity constant
	final static double PERIOD_CONSTANT=GalacticStrategyConstants.PERIOD_CONSTANT;

	final static int CLOCKWISE = 1;
	final static int COUNTERCLOCKWISE=-1;

	GenericDataSaverControl<Orbit> data_control;
	
	volatile double init_x;
	volatile double init_y;
	
	Focus focus2;
	
	volatile double cur_x;
	volatile double cur_y;
	volatile double vel_x;
	volatile double vel_y;
	
	double period;
	double time_offset;
	int direction; //1=clockwise, -1=counterclockwise - use constants CLOCKWISE and COUNTERCLOCKWISE
	
	double a;
	double b;
	double c;
	
	Orbitable<?> boss; //the object that is the other focus of this orbit
	Satellite<?> obj; //the object using this orbit
	
	public Orbit(Satellite<?> theobj, Orbitable<?> boss_obj, double focus2_x, double focus2_y, double init_x, double init_y, int dir)
	{
		data_control = new GenericDataSaverControl<Orbit>(this);
		boss=boss_obj;
		obj=theobj;
		
		direction=dir;
		this.init_x=init_x-boss.absoluteCurX();
		this.init_y=init_y-boss.absoluteCurY();
		focus2 = new Focus(focus2_x-boss.absoluteCurX(),focus2_y-boss.absoluteCurY(),this);
		
		cur_x=init_x-boss.absoluteCurX();
		cur_y=init_y-boss.absoluteCurY();
		
		calculateOrbit();
	}
	
	public synchronized void calculateOrbit()
	{
		a = (Math.hypot(init_x, init_y)+Math.hypot(focus2.getX()-init_x, focus2.getY()-init_y))/2.0d;
		
		//Calculate period.  This depends on mass.
		
		double mass_sum=boss.massSum() + obj.getMass();		
		period=PERIOD_CONSTANT*Math.sqrt(Math.pow(2*a, 3.0d))/mass_sum;
		
		//System.out.println(Double.toString(mass_sum)); //this was used for debugging
		
		calcTimeOffset();
	}
	
	public synchronized void calcTimeOffset()
	{
		c=Math.hypot(focus2.getX(),focus2.getY())/2.0d;
		b=Math.sqrt(a*a-c*c);
		
		double rot_angle;
		if(focus2.getX() != 0)
		{
			if(focus2.getX() < 0)
				rot_angle=Math.atan(focus2.getY()/focus2.getX()); //when this was negated, it was the source of the incorrect start position bug.  The inverted y measurements results in an implicit negative sign, so the added negative screwed stuff up
			else
				rot_angle=Math.PI+Math.atan(focus2.getY()/focus2.getX());
		}
		else if(focus2.getY()>0)
			rot_angle = -Math.PI/2.0d;
		else if(focus2.getY()<0)
			rot_angle = Math.PI/2.0d;
		else
			rot_angle=0; //doesn't matter, the orbit is a perfect circle
		
		double shift_x = init_x-focus2.getX()/2.0d;
		double shift_y = init_y-focus2.getY()/2.0d;
		
		double rot_x = shift_x*Math.cos(rot_angle) + shift_y*Math.sin(rot_angle);
		double rot_y = -shift_x*Math.sin(rot_angle) + shift_y*Math.cos(rot_angle);
		
		double theta = Math.acos(rot_x/a);
		if(rot_y < 0)
			theta = 2*Math.PI - theta;
		time_offset = period/(2*Math.PI)*(theta-c/a*Math.sin(theta));
	}
	
	public synchronized double absoluteCurX(){return cur_x + boss.absoluteCurX();}
	public synchronized double absoluteCurY(){return cur_y + boss.absoluteCurY();}
	
	public synchronized double absoluteInitX(){return init_x + boss.absoluteInitX();}
	public synchronized double absoluteInitY(){return init_y + boss.absoluteInitY();}
	
	public double getAbsVelX(){return vel_x + boss.getAbsVelX();}
	public double getAbsVelY(){return vel_y + boss.getAbsVelY();}
	
	public synchronized void move(double time)
	{		
		double frac_time = (time_offset + ((double)direction)*time)/period;
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
		
		double cos_theta = Math.cos(theta2);
		double sin_theta = Math.sin(theta2);
		
		double dtheta_dt = 2*Math.PI/(period*(1-c/a*cos_theta)); //the derivative of theta with respect to time, obtained via implicit differentiation
		double needs_rot_x = a*cos_theta;
		double needs_rot_y = b*sin_theta;
		double dneeds_rot_x_dt = -a*dtheta_dt*sin_theta; //the derivative of needs_rot_x with respect to time
		double dneeds_rot_y_dt = b*dtheta_dt*cos_theta; //the derivative of needs_rot_y with respect to time
		
		double rot_angle;
		if(focus2.getX() != 0)
		{
			if(focus2.getX() < 0)
				rot_angle=-Math.atan(focus2.getY()/focus2.getX());
			else
				rot_angle= Math.PI-Math.atan(focus2.getY()/focus2.getX());
		}
		else if(focus2.getY()>0)
			rot_angle = Math.PI/2;
		else if(focus2.getY()<0)
			rot_angle = -Math.PI/2;
		else
			rot_angle=0; //doesn't matter, perfect circle
		
		double needs_shift_x = needs_rot_x*Math.cos(rot_angle) + needs_rot_y*Math.sin(rot_angle);
		double needs_shift_y = -needs_rot_x*Math.sin(rot_angle) + needs_rot_y*Math.cos(rot_angle);
		
		vel_x = dneeds_rot_x_dt*Math.cos(rot_angle) + dneeds_rot_y_dt*Math.sin(rot_angle);
		vel_y = -dneeds_rot_x_dt*Math.sin(rot_angle) + dneeds_rot_y_dt*Math.cos(rot_angle);
		
		cur_x=needs_shift_x+focus2.getX()/2.0;
		cur_y=needs_shift_y+focus2.getY()/2.0;
	}
	
	//methods required for save/load
	public Orbit(){data_control = new GenericDataSaverControl<Orbit>(this);}
	public Orbitable<?> getBoss(){return boss;}
	public void setBoss(Orbitable<?> p){boss=p;}
	public Satellite<?> getObj(){return obj;}
	public void setObj(Satellite<?> o){obj=o;}
	public synchronized double getInit_x(){return init_x;}
	public synchronized void setInit_x(double x){init_x=x;}
	public synchronized double getInit_y(){return init_y;}
	public synchronized void setInit_y(double y){init_y=y;}
	public synchronized Focus getFocus2(){return focus2;}
	public synchronized void setFocus2(Focus x){focus2=x;}
	public double getCur_x(){return cur_x;}
	public void setCur_x(double x){cur_x=x;}
	public double getCur_y(){return cur_y;}
	public void setCur_y(double y){cur_y=y;}
	public synchronized double getPeriod(){return period;}
	public synchronized void setPeriod(double d){period=d;}
	public int getDirection(){return direction;}
	public void setDirection(int d){direction=d;}
	public synchronized double getA(){return a;}
	public synchronized void setA(double x){a=x;}
	public synchronized double getB(){return b;}
	public synchronized void setB(double y){b=y;}
	public synchronized double getC(){return c;}
	public synchronized void setC(double z){c=z;}

	@Override
	public GenericDataSaverControl<Orbit> getDataControl() {
		return data_control;
	}

	@Override
	public void handleDataNotSaved() {
		// TODO: is this the right thing to do?
		throw new RuntimeException("Data not yet saved... fuck.");
	}
}