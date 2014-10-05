package vh.immutableobject.example;

public class Location {

	private double x;
	private double y;

	public Location(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}

	public void setXY(double x, double y) {
		this.x = x;
		this.y = y;
	}
}
