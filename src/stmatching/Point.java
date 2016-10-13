package stmatching;

import stmatching.Time;
import map.Edge;

public class Point {
	
	public Time pointId;	//pointId is epoch (epoch time when GPS point was recorded)
	public double lon;
	public double lat;
	public Double[] location;
	public Edge edge;		//undefined
	public double v;		//GPS speed
	
	public Point(long epoch, double lon, double lat){
		this.setPointId(epoch);
		this.setLocation(lon, lat);
	}
	
	public Point(long epoch, double lon, double lat, Edge edge, double v){
		this.setPointId(epoch);
		this.setLocation(lon, lat);
		this.setEdge(edge);
		this.setV(v);
	}
	
	private void setPointId(long epoch){
		this.pointId = new Time(epoch);
	}
	
	private void setLocation(double lon, double lat){
		this.lon = lon;
		this.lat = lat;
		Double[] loc = {lon, lat};
		this.location = loc;
	}
	
	private void setEdge(Edge edge){
		this.edge = edge;
	}
	
	private void setV(double v){
		this.v = v;
	}
	
	@Override
	public boolean equals(Object point){
		if(!(point instanceof Point)) return false;
		if(this.pointId.equals(((Point) point).pointId) && 
				this.lon == ((Point) point).lon && this.lat == ((Point) point).lat) return true;
		return false;
	}
	
	@Override
	public int hashCode(){
		return this.pointId.hashCode() + ((Double) this.lon).hashCode() + ((Double) this.lat).hashCode();
	}
	
	@Override
	public String toString(){
		return this.pointId + "," + this.lon + "," + this.lat;
	}
}
