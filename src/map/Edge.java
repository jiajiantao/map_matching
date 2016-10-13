package map;

import map.Node;
import map.Speed;
import tool.Tool;

public class Edge {
	
	public class EdgeId{
		public String id;
		public EdgeId(String eid){
			this.id = eid;
		}
		
		@Override
		public boolean equals(Object edgeId){
			if(this.id.equals(((EdgeId) edgeId).id)) return true;
			return false;
		}
		
		@Override
		public int hashCode(){
			return this.id.hashCode();
		}

		@Override
		public String toString(){
			return this.id;
		}
	}
	
	public EdgeId edgeId;		//wayId + i
	public int v; 				//unit km/h
	public double length;		//unit meter
	public Node nodeStart;
	public Node nodeEnd;
	public String wayType;
	public boolean oneWay;
	
	public Edge(String eid, Node start, Node end, String type, boolean oneway){
		this.setEdgeId(eid);
		this.setStartEnd(start, end);
		this.setTypeSpeed(type);
		this.setOneWay(oneway);
		this.setLength(start, end);
	}
	
	public Edge(String eid, Node start, Node end){
		this.setEdgeId(eid);
		this.setStartEnd(start, end);
		this.setLength(start, end);
	}
	
	public Edge() {
		
	}

	private void setEdgeId(String eid){
		this.edgeId = new EdgeId(eid);
	}
	
	private void setStartEnd(Node start, Node end){
		this.nodeStart = start;
		this.nodeEnd = end;
	}
	
	private void setTypeSpeed(String type){
		this.wayType = type;
		this.v = Speed.get(type);
	}
	
	private void setOneWay(boolean oneway){
		this.oneWay = oneway;
	}
	
	private void setLength(Node start, Node end){
		this.length = Tool.getDistance(start.lat, start.lon, end.lat, end.lon);
	}
	
	@Override
	public boolean equals(Object edge){
		if(!(edge instanceof Edge)) return false;
		if(this.edgeId.equals(((Edge) edge).edgeId)) return true;
		return false;
	}
	
	@Override
	public int hashCode(){
		return this.edgeId.hashCode();
	}
	
	@Override
	public String toString(){
		return this.edgeId.id + "," + this.nodeStart.nodeId.id + "," + this.nodeEnd.nodeId.id + "," 
				+ this.wayType + "," + this.oneWay + "," + this.length + "," + this.v;
	}

}
