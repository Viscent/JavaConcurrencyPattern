package vh.immutableobject.example;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VehicleTracker {

	private  Map<String, Location> locMap 
																	= new ConcurrentHashMap<String, Location>();

	public void updateLocation(String vehicleId, Location newLocation) {
		locMap.put(vehicleId, newLocation);
	}

}
