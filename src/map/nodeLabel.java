package map;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
//import java.io.FileReader;
//import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.ArrayList;

//import sun.util.logging.resources.logging;

public class nodeLabel {
	private Double[] mmLatLon = {0.0, 0.0, 0.0, 0.0};//maxLat, minLat, maxLon, minLon
	private double threshold;
	private HashMap<String, Double> gpsDirection;
	private HashMap<String, ArrayList<Double>> nodeDirection;
	
	public static void runNodeLabel(String osmFileName, String gpsFileName, String osmFileNameOutput, String routeFileName, String outputPathAdded, HashMap<String, ArrayList<Double>> nodeDir, double TH) {
		nodeLabel nl = new nodeLabel();
		String [] gps = nl.getGPS(gpsFileName);
		nl.setThreshold(TH);
		nl.setMMLatLon(gps);
		nl.setGpsDirection(gps);
		nl.setNodeDirection(nodeDir);
		
		nl.addLabel(osmFileName, osmFileNameOutput, gps, routeFileName);
		nl.addbuspaths(osmFileNameOutput, gpsFileName, outputPathAdded);
	}

	private void addbuspaths(String osmFileNameOutput, String gpsFileName, String outputPathAdded){
		try {	 
			File file = new File(outputPathAdded);
			if (!file.exists()) {
				file.createNewFile();
			}

			//FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				    new FileOutputStream(outputPathAdded), "UTF-8"));

			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(osmFileNameOutput), "UTF8"));
				String sCurrentLine=null;
				while ((sCurrentLine = br.readLine()) != null) {
					if(!sCurrentLine.contains("<node")){
						bw.write(sCurrentLine + "\n");
					}
					else{
						break;
					}
				}
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			String lineoutput = "";
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(gpsFileName), "UTF8"));
				String sCurrentLine=null;
				sCurrentLine = br.readLine();
				int count = 0;
				int order = 0;
				while ((sCurrentLine = br.readLine()) != null) {
					String [] tokens = sCurrentLine.split(",");
					order = count;
					lineoutput = "  <node id=\""+order+"\" lat=\""+tokens[2]+"\" lon=\""+tokens[1]+"\" version=\"6\" timestamp=\"2012-03-31T04:43:27Z\" changeset=\"11162099\" uid=\"21856\" user=\"Huawei\">\n"
						         + "    <tag k=\"name\" v= \""+count+"\"/>\n"
						         + "    <tag k=\"amenity\" v=\"hospital\"/>\n"
						         + "  </node>\n";
					bw.write(lineoutput);	
					count++;
				}
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			bw.write("</osm>");	
			
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	private String [] getGPS(String gpsFileName){
		int count = 0;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(gpsFileName), "UTF8"));
			while (br.readLine() != null) {
				count++;
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
		String [] gps = new String[count];
		int i = 0;
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(gpsFileName), "UTF8"));
			String sCurrentLine=null;
			while ((sCurrentLine = br.readLine()) != null) {
				gps[i] = sCurrentLine;
				i++;
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		
		return gps; 
	}
	
	private void addLabel(String filename, String outputfilename, String [] gps, String routeFileName){
		//int count = 0;
		String [] roughRoute = new String[5000];
		int waynodecount = 0;
		
		try {	 
			File file = new File(outputfilename);
			if (!file.exists()) {
				file.createNewFile();
			}

			//FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				    new FileOutputStream(outputfilename), "UTF-8"));

			String lineoutput = "";
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF8"));
				String sCurrentLine=null;
				String head = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"
						+ "<osm version=\"0.6\" generator=\"Overpass API\"> \n"
						+ "<note>The data included in this document is from www.openstreetmap.org. The data is made available under ODbL.</note> \n"
						+ "<meta osm_base=\"2014-11-18T02:26:02Z\"/> \n"
						+ "\n"
						+ "<bounds minlat=\"22.439\" minlon=\"113.725\" maxlat=\"22.8182\" maxlon=\"114.359\"/> \n"
						+ "\n";
				bw.write(head);
				while ((sCurrentLine = br.readLine()) != null) {
					//System.out.println(sCurrentLine);
					if(sCurrentLine.contains("<node") && sCurrentLine.contains("Huawei")){
						
						String [] tokens = sCurrentLine.split(" ");
						//System.out.println(tokens[3]);
						String [] tokens1 = tokens[3].split("\"");
						//System.out.println(sCurrentLine);
						//System.out.println(tokens[4]);
						String nodeid = tokens1[1];
						
						//System.out.println(sCurrentLine+" -- " + tokens[3]+" -- " +tokens[4]+" -- " +tokens[5]+" -- " +tokens[6]);
						String [] tokensLAT = tokens[4].split("\"");
						String [] tokensLON = tokens[5].split("\"");
						boolean rangeflag = inrange(gps, tokensLAT[1], tokensLON[1], nodeid);
						//System.out.println(rangeflag);
						if(rangeflag){
							roughRoute[waynodecount] = nodeid;
							waynodecount++;
							//System.out.println(sCurrentLine);
							
							if(sCurrentLine.contains("/>")){
								String [] tokens2 = sCurrentLine.split("/");
								//System.out.println(tokens2[0]);
								String firstpart = tokens2[0] + ">";
								lineoutput = firstpart + "\n" 
										+ "\t<tag k=\"name\" v= \"" + nodeid + "\"/>\n" 
										+ "\t<tag k=\"name:fr\" v= \"Candidate\"/>\n"
										+ "\t<tag k=\"natural\" v=\"peak\"/>\n"
										+ "  </node>\n";
							}
							else{
								lineoutput = sCurrentLine + "\n" 
										+ "\t<tag k=\"name\" v= \"" + nodeid + "\"/>\n"
										+ "\t<tag k=\"name:fr\" v= \"Candidate\"/>\n"
										+ "\t<tag k=\"natural\" v=\"peak\"/>\n"
										+ "  </node>\n";
							}	
							bw.write(lineoutput);
						}
					}					
				}
				String tail = "</osm>";
				bw.write(tail);
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}			
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
		savetoFile(roughRoute, routeFileName);
	}
	
	private void savetoFile(String [] roughRoute, String routeFileName){
		try {	 
			File file = new File(routeFileName);
			if (!file.exists()) {
				file.createNewFile();
			}

			//FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(routeFileName), "UTF-8"));
			for(int i = 0; i < roughRoute.length; i++){
				if(roughRoute[i] != null){
					bw.write(roughRoute[i]);
					bw.newLine();
				}
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
		
	}
	
	private boolean inrange(String [] gps, String LAT, String LON, String nodeid){
		double lat = Double.parseDouble(LAT);
		double lon = Double.parseDouble(LON);
		boolean flag = false;
		if(outRange(lat, lon, mmLatLon)) return false;
		for(int i = 1; i < gps.length; i++){
			String [] tokens = gps[i].split(",");
			double gpsLat = Double.parseDouble(tokens[2]); 
			double gpsLon = Double.parseDouble(tokens[1]);
					
			if(Math.abs(gpsLat - lat)<threshold && Math.abs(gpsLon - lon)<threshold){
				//System.out.println(gpsLat + " "+ lat + " "+ gpsLon + " "+ lon);
				if(isSameDirection(gpsLon, gpsLat, nodeid)){
					flag = true;
					break;
				}
			}
		}
		
		if(flag){
			return true;
		}
		else{
			return false;
		}
	}
	
	private void setMMLatLon(String[] gps){
		double maxLon = 0;
		double maxLat = 0;
		double minLon = 10000;
		double minLat = 10000;
		for(int i = 1; i < gps.length; i++){
			String [] tokens = gps[i].split(",");
			double gpsLat = Double.parseDouble(tokens[2]); 
			double gpsLon = Double.parseDouble(tokens[1]);
			maxLat = (gpsLat > maxLat)? gpsLat:maxLat;
			minLat = (gpsLat < minLat)? gpsLat:minLat;
			maxLon = (gpsLon > maxLon)? gpsLon:maxLon;
			minLon = (gpsLon < minLon)? gpsLon:minLon;
		}
		mmLatLon[0] = maxLat;
		mmLatLon[1] = minLat;
		mmLatLon[2] = maxLon;
		mmLatLon[3] = minLon;
	}
	
	private void setGpsDirection(String[] gps){
		gpsDirection = new HashMap<String, Double>();
		for(int i = 1; i < gps.length - 1; i++){
			String[] tokens1 = gps[i].split(",");
			String[] tokens2 = gps[i+1].split(",");
			double gpsLat1 = Double.parseDouble(tokens1[2]); 
			double gpsLon1 = Double.parseDouble(tokens1[1]);
			double gpsLat2 = Double.parseDouble(tokens2[2]); 
			double gpsLon2 = Double.parseDouble(tokens2[1]);
			Double[] lonLat1 = {gpsLon1, gpsLat1};
			Double[] lonLat2 = {gpsLon2, gpsLat2};
			double direction = Main.getDirection(lonLat1, lonLat2);
			String key = gpsLon1 + "," + gpsLat1;
			gpsDirection.put(key, direction);
		}
		String[] tokensLast = gps[gps.length - 1].split(",");
		String[] tokensLast_1 = gps[gps.length - 2].split(",");
		String keyLast = tokensLast[2] + "," + tokensLast[1];
		String keyLast_1 = tokensLast_1[2] + "," + tokensLast_1[1];
		gpsDirection.put(keyLast, gpsDirection.get(keyLast_1));
	}
	
	private boolean isSameDirection(double gpsLon, double gpsLat, String nodeid){
		if(!this.nodeDirection.containsKey(nodeid)) return true;
		ArrayList<Double> dirList = this.nodeDirection.get(nodeid);
		if(!this.gpsDirection.containsKey(gpsLon + "," + gpsLat)) return true;
		double gpsDir = this.gpsDirection.get(gpsLon + "," + gpsLat);
		for(int index = 0; index < dirList.size(); index++){
			double nodeDir = dirList.get(index);
			double angle = Math.abs(nodeDir - gpsDir);
			if(angle > 180) angle = 360 - angle;
			if(angle < 90) return true;
		}
		return false;
	}
	
	private void setNodeDirection(HashMap<String, ArrayList<Double>> nodeDir){
		this.nodeDirection = nodeDir;
	}
	
	private void setThreshold(double TH){
		this.threshold = TH;
	}
	
	private boolean outRange(double lat, double lon, Double[] mmLatLon){
		//maxLat, minLat, maxLon, minLon
		if(lat >= mmLatLon[0] + 0.001 || lat <= mmLatLon[1] - 0.001 || lon >= mmLatLon[2] + 0.001 || lon <= mmLatLon[3] - 0.001)
			return true;
		else return false;
			
	}
}
