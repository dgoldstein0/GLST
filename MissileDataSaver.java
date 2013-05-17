
public class MissileDataSaver extends FlyerDataSaver<Missile> {

	boolean target_alive;
	
	protected void doSaveMoreData(Missile m){target_alive = m.target_alive;}
	protected void doLoadMoreData(Missile m){m.target_alive = target_alive;}
}
