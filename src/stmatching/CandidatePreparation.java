package stmatching;

import map.Edge;
import map.Map;
import stmatching.Time;
import stmatching.Candidate;
import stmatching.Point;
import tool.Tool;

import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CandidatePreparation {
	private int searchRadius = 100; 			//unit meter
	private int maxCandidateNo = 5;
	private ArrayList<Time> times;			
	private HashMap<Time, ArrayList<Candidate>> candidates;
	private HashMap<Time, Point> points;	
	private double MINLON;
	private double MAXLON;
	private double MINLAT;
	private double MAXLAT;
	//for ddldmatching
	private HashMap<Time, Integer> nRoadSegmentS; 	//F1
	private HashMap<Time, Integer> nRoadTypeS;		//F2

	public CandidatePreparation(String gpsFile, Map map) throws IOException{
		this.setPoints(gpsFile);
		this.setCandidates(gpsFile, map);
	}
	
	public CandidatePreparation(String gpsFile, Map map, int sRadius, int mCanNo) throws IOException{
		this.setSearchRadius(sRadius);
		this.setMaxCandidateNo(mCanNo);
		this.setPoints(gpsFile);
		this.setCandidates(gpsFile, map);
		this.setGpsRange();
	}
	
	public CandidatePreparation(String gpsFile, Map map, int sRadius, int mCanNo, int pointGap) throws IOException{
		this.setSearchRadius(sRadius);
		this.setMaxCandidateNo(mCanNo);
		this.setPoints(gpsFile, pointGap);
		this.setCandidates(gpsFile, map);
		this.setGpsRange();
	}
	
	public CandidatePreparation(String gpsFile, Map map, int sRadius, int mCanNo, int pointGap, int bias) throws IOException{
		this.setSearchRadius(sRadius);
		this.setMaxCandidateNo(mCanNo);
		this.setPoints(gpsFile, pointGap, bias);
		this.setCandidates(gpsFile, map);
		this.setGpsRange();
	}
	
	private void setSearchRadius(int sRadius){
		this.searchRadius = sRadius;
	}
	
	private void setMaxCandidateNo(int mCanNo){
		this.maxCandidateNo = mCanNo;
	}
	
	private void setCandidates(String gpsFile, Map map) throws IOException{
		this.candidates = new HashMap<Time, ArrayList<Candidate>>();
		this.nRoadSegmentS = new HashMap<Time, Integer>();
		this.nRoadTypeS = new HashMap<Time, Integer>();
		//setGpsRange();
		//2. get edge (nearest point) within searchRange and compute candidate
		for(int index = 0; index < this.times.size(); index++){
			Time pid = this.times.get(index);
			Point point = this.points.get(pid);
			HashMap<String, Candidate> xydCand = new HashMap<String, Candidate>();
			HashSet<Edge> edgeInCircle = new HashSet<Edge>();
			HashSet<String> edgeTypeInCircle = new HashSet<String>();
			for(Edge edge: map.edgeSet){
				if(!inRange(edge, point)) continue;
				Candidate candidate = computeCandidate(point, edge);
				if(candidate.distance > this.searchRadius) continue;
				String xyd = candidate.lon + "," + candidate.lat + "," + candidate.distance;
				xydCand = renewXydCand(xydCand, xyd, candidate, edge);
				edgeInCircle.add(edge);
				edgeTypeInCircle.add(edge.wayType);
			}
			ArrayList<Candidate> topMaxNoCand = getTopMaxNoCandidates(xydCand);
			addCandidates(topMaxNoCand, pid);
			this.nRoadSegmentS.put(pid, edgeInCircle.size());
			this.nRoadTypeS.put(pid, edgeTypeInCircle.size());
		}
	}
	
	public static Candidate computeCandidate(Point point, Edge edge){
		//known conditions
		double x1_1 = edge.nodeStart.lon;
		double y1_1 = edge.nodeStart.lat;
		double x1_2 = edge.nodeEnd.lon;
		double y1_2 = edge.nodeEnd.lat;
		double x2_1 = point.lon;
		double y2_1 = point.lat;
		//unknown parameters
		double k1 = 0;
		double b1 = 0;
		double k2 = 0;
		double b2 = 0;
		double x2_2 = 0;
		double y2_2 = 0;
		//two orthogonal lines
		if((x1_2 - x1_1) == 0){		 //edge is vertical line
			x2_2 = x1_1;
			y2_2 = y2_1;
		}else if((y1_2 - y1_1) == 0){//edge is horizontal line
			x2_2 = x2_1;
			y2_2 = y1_1;
		}else{						 //edge is oblique line
			k1 = (y1_2 - y1_1)/(x1_2 - x1_1);
			b1 = y1_1 - k1*x1_1;
			k2 = -1/k1;
			b2 = y2_1 - k2*x2_1;
			//compute orthogonal point
			x2_2 = -(b2 - b1)/(k2 - k1);
			y2_2 = k2*x2_2 + b2;
		}
		//Judge whether (x2_2, y2_2) lies within the edge line segment. 
		//If so, set candidate with (x2_2, y2_2). 
		//Otherwise set candidate as start/end that is nearer to (x2_2, y2_2)
		double lon = 0;
		double lat = 0;
		double alpha = -1;
		Double[] maxlon_lat = {0.0, 0.0};
		Double[] minlon_lat = {0.0, 0.0};
		Double[] maxlat_lon = {0.0, 0.0};
		Double[] minlat_lon = {0.0, 0.0};
		if(x1_1 > x1_2){
			maxlon_lat[0] = x1_1;
			maxlon_lat[1] = y1_1;
		}else{
			maxlon_lat[0] = x1_2;
			maxlon_lat[1] = y1_2;
		}
		if(x1_1 < x1_2){
			minlon_lat[0] = x1_1;
			minlon_lat[1] = y1_1;
		}else{
			minlon_lat[0] = x1_2;
			minlon_lat[1] = y1_2;
		}
		if(y1_1 > y1_2){
			maxlat_lon[0] = y1_1;
			maxlat_lon[1] = x1_1;
		}else{
			maxlat_lon[0] = y1_2;
			maxlat_lon[1] = x1_2;
		}
		if(y1_1 < y1_2){
			minlat_lon[0] = y1_1;
			minlat_lon[1] = x1_1;
		}else{
			minlat_lon[0] = y1_2;
			minlat_lon[1] = x1_2;
		}
		if(x1_2 - x1_1 == 0){			//edge is vertical
			lon = x1_2;
			if(y2_2 >= maxlat_lon[0]){
				//set candidate to max
				lat = maxlat_lon[0];
				alpha = (y1_1 == maxlat_lon[0])? 1:0;
			}else if(y2_2 <= minlat_lon[0]){
				//set candidate to min
				lat = minlat_lon[0];
				alpha = (y1_1 == minlat_lon[0])? 1:0;
			}else{
				//set candidate to orthogonal point
				lat = y2_2;
				if(y1_2 - y1_1 != 0) alpha = (y1_2 - lat)/(y1_2 - y1_1);
				else return new Candidate(0l, 0.0, 0.0, Double.MAX_VALUE, null);
			}
		}else if(x2_2 > maxlon_lat[0]){		//edge is not vertical
			//set candidate to max
			lon = maxlon_lat[0];
			lat = maxlon_lat[1];
			alpha = (x1_1 == maxlon_lat[0])? 1:0;
		}else if(x2_2 < minlon_lat[0]){		//edge is not vertical
			//set candidate to min
			lon = minlon_lat[0];
			lat = minlon_lat[1];
			alpha = (x1_1 == minlon_lat[0])? 1:0;
		}
		else{							//edge is not vertical
			//set candidate to orthogonal point
			lon = x2_2;
			lat = y2_2;
			alpha = (x1_2 - lon)/(x1_2 - x1_1);
		}
		double distance = Tool.getDistance(point.lat, point.lon, lat, lon);
		HashMap<Edge, Double> edgeAlpha = new HashMap<Edge, Double>();
		edgeAlpha.put(edge, alpha);
		return new Candidate(point.pointId.epoch, lon, lat, distance, edgeAlpha);
	}
	
	private boolean inRange(Edge edge, Point point){
		if(edge.nodeStart.lon < (point.lon - 0.005) && edge.nodeEnd.lon < (point.lon - 0.005)) return false;
		if(edge.nodeStart.lon > (point.lon + 0.005) && edge.nodeEnd.lon > (point.lon + 0.005)) return false;
		if(edge.nodeStart.lat < (point.lat - 0.005) && edge.nodeEnd.lat < (point.lat - 0.005)) return false;
		if(edge.nodeStart.lat > (point.lat + 0.005) && edge.nodeEnd.lat > (point.lat + 0.005)) return false;
		return true;
	}

	private HashMap<String, Candidate> renewXydCand(HashMap<String, Candidate> xydCand, String xyd, Candidate newCand, Edge edge){
		if(!xydCand.containsKey(xyd)) xydCand.put(xyd, newCand);
		else{
			Candidate oldCand = xydCand.get(xyd);
			oldCand.edgeAlpha.put(edge, newCand.edgeAlpha.get(edge));
		}
		return xydCand;
	}
	
	private ArrayList<Candidate> getTopMaxNoCandidates(HashMap<String, Candidate> xydCand){
		ArrayList<Candidate> topMaxNoCand = new ArrayList<Candidate>();
		ArrayList<Double> dList = new ArrayList<Double>();
		HashSet<Double> dSet = new HashSet<Double>();
		for(String xyd: xydCand.keySet()) dSet.add(Double.parseDouble(xyd.split(",")[2]));
		for(Iterator<Double> it = dSet.iterator(); it.hasNext();) dList.add(it.next());		
		Collections.sort(dList);
		int indexCand = 0;
		for(int index = 0; index < dList.size(); index++){
			String distance = Double.toString(dList.get(index));
			for(String xyd: xydCand.keySet()){
				if((xyd.split(",")[2]).equals(distance)){
					indexCand++;
					Candidate candidate = xydCand.get(xyd);
					candidate.setIndex(indexCand);
					topMaxNoCand.add(candidate); 
				}
				if(topMaxNoCand.size() >= this.maxCandidateNo) break;
			}
			if(topMaxNoCand.size() >= this.maxCandidateNo) break;
		}
		return topMaxNoCand;
	}
	
	private void addCandidates(ArrayList<Candidate> candList, Time time){
		this.candidates.put(time, candList);	
	}
	
	private void setPoints(String gpsFile) throws IOException{
		this.points = new HashMap<Time, Point>();
		this.times = new ArrayList<Time>();
		BufferedReader br = new BufferedReader(new FileReader(gpsFile));
		String sBuf = null;
		while((sBuf = br.readLine()) != null){
			long epoch = Long.parseLong(sBuf.split(",")[11]);
			double lon = Double.parseDouble(sBuf.split(",")[7]);
			double lat = Double.parseDouble(sBuf.split(",")[8]);
			double v = Double.parseDouble(sBuf.split(",")[12]);
			this.addTimeStamp(epoch);
			Point point = new Point(epoch, lon, lat, null, v);
			points.put(point.pointId, point);
		}
		br.close();
	}
	
	private void setPoints(String gpsFile, int pointGap) throws IOException{
		this.points = new HashMap<Time, Point>();
		this.times = new ArrayList<Time>();
		BufferedReader br = new BufferedReader(new FileReader(gpsFile));
		String sBuf = null;
		int i = -1;
		while((sBuf = br.readLine()) != null){
			i++;
			if(i%(pointGap+1) != 0)
				continue;
			long epoch = Long.parseLong(sBuf.split(",")[11]);
			double lon = Double.parseDouble(sBuf.split(",")[7]);
			double lat = Double.parseDouble(sBuf.split(",")[8]);
			double v = Double.parseDouble(sBuf.split(",")[12]);
			this.addTimeStamp(epoch);
			Point point = new Point(epoch, lon, lat, null, v);
			points.put(point.pointId, point);
		}
		br.close();
	}
	
	
	private void setPoints(String gpsFile, int pointGap, int bias) throws IOException{
		this.points = new HashMap<Time, Point>();
		this.times = new ArrayList<Time>();
		BufferedReader br = new BufferedReader(new FileReader(gpsFile));
		String sBuf = null;
		int i = -1;
		while((sBuf = br.readLine()) != null){
			i++;
			if((i-bias)%(pointGap+1) != 0)
				continue;
			long epoch = Long.parseLong(sBuf.split(",")[11]);
			double lon = Double.parseDouble(sBuf.split(",")[7]);
			double lat = Double.parseDouble(sBuf.split(",")[8]);
			double v = Double.parseDouble(sBuf.split(",")[12]);
			this.addTimeStamp(epoch);
			Point point = new Point(epoch, lon, lat, null, v);
			points.put(point.pointId, point);
		}
		br.close();
	}
	
	private void setGpsRange(){
		this.MINLON = Double.MAX_VALUE;
		this.MINLAT = Double.MAX_VALUE;
		this.MAXLON = Double.MIN_VALUE;
		this.MAXLAT = Double.MIN_VALUE;
		for(Time pid: this.points.keySet()){
			double lon = this.points.get(pid).lon;
			double lat = this.points.get(pid).lat;
			this.MINLON = (this.MINLON <= lon)? this.MINLON:lon;
			this.MINLAT = (this.MINLAT <= lat)? this.MINLAT:lat;
			this.MAXLON = (this.MAXLON >= lon)? this.MAXLON:lon;
			this.MAXLAT = (this.MAXLAT >= lat)? this.MAXLAT:lat;
		}
		this.MINLON -= 0.005;	//500m
		this.MINLAT -= 0.005;	//500m
		this.MAXLON += 0.005;	//500m
		this.MAXLAT += 0.005;	//500m
	}
	
	private void addTimeStamp(long epoch){
		if(this.times.size() > 0 && epoch <= this.times.get(this.times.size() - 1).epoch) return;
		Time time = new Time(epoch);
		this.times.add(time);
	}
	
	public HashMap<Time, ArrayList<Candidate>> getCandidates(){
		return this.candidates;
	}
	
	public HashMap<Time, Point> getPoints(){
		return this.points;
	}
	
	public ArrayList<Time> getTimes(){
		return this.times;
	}
	
	public double getMAXLON(){
		return this.MAXLON;
	}
	
	public double getMINLON(){
		return this.MINLON;
	}
	
	public double getMAXLAT(){
		return this.MAXLAT;
	}
	
	public double getMINLAT(){
		return this.MINLAT;
	}
	
	public HashMap<Time, Integer> getNRoadSegmentS(){
		return this.nRoadSegmentS;
	}
	
	public HashMap<Time, Integer> getNRoadTypeS(){
		return this.nRoadTypeS;
	}
	
}
