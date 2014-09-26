package vh.activeobject.lib;

public class SampleActiveObjectImpl {

	public String doProcess(String arg, int i) {
		try {
			//模拟一个比较耗时的操作
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return arg + "-" + i;
	}

}
