import java.util.Random;


public class MathFormula {
	public static double randomize(double input){
		Random now = new Random();
		double output;
		output=now.nextGaussian();
		output*=(GalacticStrategyConstants.rand_mod*input);
		output+=input;
		return output;
	}
	
}
