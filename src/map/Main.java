package map;

import java.io.*;
import java.util.regex.*;
import java.util.*;
import java.text.*;

import map.nodeLabel;

public class Main{
	private static HashMap<String, ArrayList<Double>> nodeDirection;
	private static HashMap<String, Double[]> nodeLonLat;

	public static void main(String[] args) throws IOException{		
		log("Start...");
		String osmFileName = "./SZmapLarge_waynodemarked.OSM";
		setNodeLonLat(osmFileName);
		setNodeDirection(osmFileName);
		log("Done: generate oneway node direction.");
		
		double threshold = 0.0003;
		int start = 0;//Integer.parseInt(args[0]);// 
		int end = 3;//Integer.parseInt(args[1]);//
		String routeFile = "./routes.txt";
		ArrayList<String> routeSet = getRouteSet(routeFile);
		for(int index = start; index < end; index++){
			String route = routeSet.get(index);
			String gpsFileName = "./Busroute_extra50_folds/" + route + "/" + route + ".txt";
			String osmFileNameOutput = "./Busroute_extra50_folds/" + route + "/mapWithRoute.OSM";
			String routeFileName = "./Busroute_extra50_folds/" + route + "/Route.OSM";
			String outputPathAdded = "./Busroute_extra50_folds/" + route + "/routeGPSsample.OSM";
			nodeLabel.runNodeLabel(osmFileName, gpsFileName, osmFileNameOutput, routeFileName, outputPathAdded, nodeDirection, threshold);
			log("Done " + index + ": generate route " + route);
		}
		
		log("All done!");
	}
	
	private static void setNodeLonLat(String file) throws IOException{
		nodeLonLat = new HashMap<String, Double[]>();
		BufferedReader br = new BufferedReader(new FileReader(file));
		String sBuf = null;
		while((sBuf = br.readLine()) != null){
			Matcher m = Pattern.compile("<node id=\"(.*?)\" lat=\"(.*?)\" lon=\"(.*?)\"").matcher(sBuf);
			if(m.find()){
				String node = m.group(1);
				Double[] lonLat = {0.0, 0.0};
				lonLat[1] = Double.parseDouble(m.group(2));
				lonLat[0] = Double.parseDouble(m.group(3));
				nodeLonLat.put(node, lonLat);
			}
		}
		br.close();
	}
	
	private static void setNodeDirection(String file) throws IOException{
		nodeDirection = new HashMap<String, ArrayList<Double>>();
		String way = "";
		boolean wayFlag = false;
		BufferedReader br = new BufferedReader(new FileReader(file));
		String sBuf = null;
		while((sBuf = br.readLine()) != null){
			if(sBuf.contains("<way")){
				wayFlag = true;
				way = "";
			}
			if(sBuf.contains("</way>")){
				wayFlag = false;
				way += sBuf; 
				if(way.contains("oneway")){
					addNodeDirection(way, file);
				}
			}
			if(wayFlag) way += sBuf + "\n";
		}
		br.close();
	}
	
	private static void addNodeDirection(String oneway, String file) throws IOException{
		ArrayList<String> waynodes = new ArrayList<String>();
		String regex = "<nd ref=\"(.*?)\"/>";
		Matcher m = Pattern.compile(regex).matcher(oneway);
		while(m.find()){
			String nodeId = m.group(1);
			waynodes.add(nodeId);
		}
		for(int index = 0; index < waynodes.size(); index++){	
			String nodeId1 = waynodes.get(index);
			if(!nodeDirection.containsKey(nodeId1)) nodeDirection.put(nodeId1, new ArrayList<Double>());
			if(index + 1 < waynodes.size()){
				String nodeId2 = waynodes.get(index + 1);
				Double[] lonLat1 = getLonLat(nodeId1);
				Double[] lonLat2 = getLonLat(nodeId2);
				Double direction = getDirection(lonLat1, lonLat2);
				nodeDirection.get(nodeId1).add(direction);
			}else{// nodeId1 is last node on the way. give it the same direction as previous node.
				String nodeId0 = waynodes.get(index - 1);
				int n = nodeDirection.get(nodeId0).size();
				Double direction = nodeDirection.get(nodeId0).get(n - 1);
				nodeDirection.get(nodeId1).add(direction);
			}
		}
	}
	
	private static Double[] getLonLat(String node) throws IOException{
		if(nodeLonLat.containsKey(node)) return nodeLonLat.get(node);
		return null;
	}
	
	public static Double getDirection(Double[] lonLat1, Double[] lonLat2){
		double lon1 = lonLat1[0];
		double lat1 = lonLat1[1];
		double lon2 = lonLat2[0];
		double lat2 = lonLat2[1];
		double deltaLon = lon2 - lon1;
		double deltaLat = lat2 - lat1;
		return Math.atan2(deltaLat, deltaLon)/Math.PI*180;
	}
	
	private static ArrayList<String> getRouteSet(String routeFile) throws IOException{
		ArrayList<String> routeSet = new ArrayList<String>();
		BufferedReader br = new BufferedReader(new FileReader(routeFile));
		String sBuf = null;
		while((sBuf = br.readLine()) != null){
			String route = sBuf.split(",")[1];
			routeSet.add(route);
		}
		br.close();
		return routeSet;
	}
	
	public static void log(String s){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date();
		System.out.println("[" + sdf.format(date) + "] " + s);
	}

}
