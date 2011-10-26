
public class InvalidOrder extends Order {

	@Override @Deprecated
	public void execute(Galaxy g) throws DataSaverControl.DataNotYetSavedException {
		// TODO Auto-generated method stub
		throw new RuntimeException("Executing an InvalidOrder == BAD!");
	}

}
