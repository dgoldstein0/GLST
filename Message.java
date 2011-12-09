
public class Message {

	static enum Type {ORDER, DECISION};
	
	Type type;
	Object contents;
	
	Message(Type t, Object o)
	{
		type = t;
		contents = o;
	}
	
	Message(){}
	public Type getType(){return type;}
	public Object getContents(){return contents;}
	public void setType(Type t){type = t;}
	public void setContents(Object o) {contents = o;}
}
