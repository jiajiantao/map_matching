package astar;

import java.util.*;

import map.Edge;

public class ShortestPath {
	public double length;
	public ArrayList<Edge> path;
	
	public ShortestPath(){
		
	}
	
	public ShortestPath(double length, ArrayList<Edge> path){
		this.length = length;
		this.path = path;
	}
	
	@Override
	public String toString(){
		String edges = "";
		for(int index = 0; index < this.path.size(); index++){
			edges += "," + this.path.get(index).edgeId;
		}
		return this.length + edges;
	}
}
