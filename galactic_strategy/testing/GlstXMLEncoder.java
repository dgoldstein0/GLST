package galactic_strategy.testing;

import java.beans.DefaultPersistenceDelegate;
import java.beans.XMLEncoder;
import java.io.OutputStream;

public class GlstXMLEncoder extends XMLEncoder {

	GlstXMLEncoder(OutputStream os) {
		super(os);
		
		setPersistenceDelegate(
				galactic_strategy.sync_engine.DataSaverControl.class,
				new DefaultPersistenceDelegate(new String[]{"the_obj"})
			);
	}
}
