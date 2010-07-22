
public class RelaxedDataSaverTester {

	static int[] times = {	5,		18,		31,		54,		78,
							131,	149,	150,	159,	200,
							217,	240,	255,	281,	299,
							313,	340,	389,	400,	402,
							450,	460,	479,	510,	514,
							
							551,	570,	600,	660,	720,
							890,	901,	904,	940,	990,
							1002,	1005,	1010,	1040,	1050,
							1058,	1060,	1070,	1083,	1131,
							1142,	1149,	1170,	1189, 	1300,
							
							1350,	1420,	1489, 	1520,	1552,
							1660,	1667};
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Planet p = new Planet(0,"tester", 100.0, 1000.0, 10, 10.0, .00001);
		p.owner = new Player();
		
		p.data_control.saveData();
		
		for(int i=0; i<times.length; i++)	
		{
			p.time=times[i];
			p.data_control.saveData();
		}
		
		p.data_control.revertToTime(1320);
		System.out.println(p.time);
	}

}
