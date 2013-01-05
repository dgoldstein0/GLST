
public class Message {

	public static enum Type {ORDER, DECISION};
	
	Type type;
	Order contents;
	int sender_id;
	
	public Message(Type t, Order o)
	{
		type = t;
		contents = o;
		sender_id = GameInterface.GC.player_id;
	}
	
	public Message(){}
	public Type getType(){return type;}
	public Order getContents(){return contents;}
	public int getSender_id(){return sender_id;}
	public void setType(Type t){type = t;}
	public void setContents(Order o) {contents = o;}
	public void setSender_id(int s){sender_id = s;}
}
