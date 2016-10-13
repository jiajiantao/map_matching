package map;

import java.util.HashMap;

public class Speed{
	public static HashMap<String, Integer> speedType;	//unit km/h
	static {
		//Roads
		speedType = new HashMap<String, Integer>();
		speedType.put("motorway", 100);
		speedType.put("trunk", 80);
		speedType.put("primary", 70);
		speedType.put("secondary", 60);
		speedType.put("tertiary", 50);
		speedType.put("unclassified", 40);
		speedType.put("residential", 30);
		speedType.put("service", 20);
		//Link roads
		speedType.put("motorway_link", 20);
		speedType.put("trunk_link", 20);
		speedType.put("primary_link", 20);
		speedType.put("secondary_link", 20);
		speedType.put("tertiary_link", 20);
		//Special road types
		speedType.put("bus_guideway", 20);
		speedType.put("road", 20);
	}
	
	public static int get(String wayType){
		if(speedType.containsKey(wayType)) return speedType.get(wayType);
		else return 55;
	}
}
