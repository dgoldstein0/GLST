
public class CollisionAvoidingAI extends FlyerAI {

	@Override
	public double calcDesiredDirection(long t) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double calcDesiredSpeed(long t, double dirChng) {
		
		/*new_v_x_a = v_x_a;
		new_v_y_a = v_y_a;
		if (proj_frac_a > proj_frac_b)
		{
			new_v_x_a += proj_frac_a*dif_x;
			new_v_y_a += proj_frac_a*dif_y;
		}
		else
		{
			new_v_x_a = v_x_a - proj_frac_b*dif_x;
			new_v_y_a = v_y_a - proj_frac_b*dif_y;
		}
		
		a.speed = Math.hypot(new_v_x_a, new_v_y_a);
		if (Math.abs(new_v_y_a) > 0.0001 || Math.abs(new_v_x_a) > 0.0001)
			a.direction = Math.atan2(new_v_y_a, new_v_x_a);*/
		
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int directionType() {
		// TODO Auto-generated method stub
		return 0;
	}

}
