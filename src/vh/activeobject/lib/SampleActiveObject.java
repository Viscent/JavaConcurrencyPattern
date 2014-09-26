package vh.activeobject.lib;

import java.util.concurrent.Future;

public interface SampleActiveObject {
	public Future<String> process(String arg, int i);
}