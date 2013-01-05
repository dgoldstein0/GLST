import java.util.Random;


public class MathFormula {
	public static double randomize(double mean, double StDev){
		Random now = new Random();
		double output;
		output=now.nextGaussian();
		output*=StDev;
		output+=mean;
		return output;
	}
	public static double SumofSquares(double deltaX,double deltaY){
		return (deltaX*deltaX + deltaY*deltaY);
	}
}
