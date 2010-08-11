
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
							1142,	1200,	1300,	1400,	1500, 1501};
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		Planet p = new Planet(0,"tester", 100.0, 1000.0, 10, 10.0, .00001);
		p.owner = new Player();
		
		p.data_control.saveData();
		
		for(int i=0; i<times.length; i++)	
		{
			p.time=times[i];
			p.data_control.saveData();
		}
		
		int indx = p.data_control.getIndexForTime(17);
		System.out.println("indx = " + Integer.toString(indx));
		System.out.println(p.data_control.saved_data[indx].t);

		try {
			p.data_control.revertToTime(17);
		} catch (DataSaverControl.DataNotYetSavedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(p.time);
	}

}
