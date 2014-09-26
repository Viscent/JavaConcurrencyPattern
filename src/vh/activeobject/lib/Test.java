package vh.activeobject.lib;

import java.util.concurrent.*;

public class Test {

	public static void main(String[] args) throws 
												InterruptedException, ExecutionException {

		SampleActiveObject sao = ActiveObjectProxy.newInstance(
		    SampleActiveObject.class, new SampleActiveObjectImpl(),
		    Executors.newCachedThreadPool());
		Future<String> ft=null;
    try {
	    ft = sao.process("Something", 1);
    } catch (Exception e) {
	    e.printStackTrace();
    }
		Thread.sleep(500);
		System.out.println(ft.get());
	}
}
