package astar;

import java.util.*;

import tool.Tool;
import map.Edge;
import map.Node;
import map.Map;

public class Astar {
	private double shortestLength;
	private ArrayList<Edge> shortestPath;
	private ShortestPath SP;
	private ArrayList<NodeAttribute> openSet;
	private HashMap<Node, Attribute> closeSet;
	private HashMap<Node, Attribute> openNodeAttribute;
	private int RANGE = 2;
	
	public Astar(Node source, Node sink, Map map, double distanceSS /* distanceSS is parameter of ellipsoid*/){
		if(source.equals(sink)){
			//source and sink are same node, set spLength to 0 and spPath empty.
			this.shortestLength = 0;
			this.shortestPath = new ArrayList<Edge>();
			this.setSP();
		}else{
			this.openSet = new ArrayList<NodeAttribute>();
			// openSet's meaning index starts from 1. So set openSet(0) to null.
			this.openSet.add(new NodeAttribute(null, null, null, 0, 0, 0));
			this.closeSet = new HashMap<Node, Attribute>();
			this.openNodeAttribute = new HashMap<Node, Attribute>();
			this.runAstar(source, sink, map, distanceSS);
			this.decodeSP(source, sink);
		}
	}
	
	private void runAstar(Node source, Node sink, Map map, double distanceSS){
		HashMap<Node, ArrayList<Edge>> outEdges = map.outEdges;
		this.addOpenSet(source, new Attribute(null, null, 0, 0, 0));
		while(this.openSet.size() > 1){
			NodeAttribute parent_attribute = this.getMinFInOpen();
			if(parent_attribute == null)
				break;
			Node parent = parent_attribute.node;
			Attribute parAtt = parent_attribute.attribute;
			this.removeOpenSet();
			this.closeSet.put(parent, parAtt);
			//Sink is put into the closeSet, meaning shortest path is found. 
			if(parent.equals(sink)) 
				break;
			if(!outEdges.containsKey(parent)) 
				continue;
			ArrayList<Edge> edgeList = outEdges.get(parent);
			for(int index = 0; index < edgeList.size(); index++){
				Edge edge = edgeList.get(index);
				Node neighbor = edge.nodeEnd;
				double blockLength = edge.length;
				if(!outEdges.containsKey(neighbor))
					continue;
				ArrayList<Edge> neighborEdgeList = outEdges.get(neighbor);
				int loopNode = 0;
				//********** Accelerate A-star ************
				while(neighborEdgeList.size() == 1 && neighborEdgeList.get(0).oneWay == true){
					// It is only an intermediate node. Continue until find a cross node.
					loopNode++;
					edge = neighborEdgeList.get(0);
					neighbor = edge.nodeEnd;
					blockLength += edge.length;
					if(loopNode > 50 || neighbor.equals(sink) || !outEdges.containsKey(edge.nodeEnd))
						break;
					neighborEdgeList = outEdges.get(edge.nodeEnd);
				}
				//****************************************
				if(Tool.getDistance(source.location, neighbor.location) + Tool.getDistance(sink.location, neighbor.location) > this.RANGE * distanceSS)
					//Ignore neighbor out of the ellipsoid.
					continue;
				if(this.closeSet.containsKey(neighbor))
					//Ignore neighbor in closeSet.
					continue;
				if((!openSetContains(neighbor)) || openSetGet(neighbor).g_value > (parAtt.g_value + blockLength)){
					//Update the neighbor inside the openSet and with larger g_value than that through parent.
					//Put in openSet the neighbor inside the ellipsoid, but out of the closeSet and openSet.
					double g_value = parAtt.g_value + blockLength;
					double h_value = Tool.getDistance(neighbor.location, sink.location);
					double f_value = g_value + h_value;
					Attribute sonAtt = new Attribute(parent, edge, g_value, h_value, f_value);
					this.addOpenSet(neighbor, sonAtt);
					continue;
				}else{
					//Ignore neighbor inside the openSet but with smaller g_value than that through parent.
					continue;
				}
			}//endfor
		}
	}
	
	private void decodeSP(Node source, Node sink){
		if(this.closeSet.containsKey(sink)){
			this.shortestLength = this.closeSet.get(sink).g_value;
			this.shortestPath = new ArrayList<Edge>();
			Node child = sink;
			while(!child.equals(source)){
				Node parent = this.closeSet.get(child).parent;
				Edge parEdge = this.closeSet.get(child).parentEdge;
				this.shortestPath.add(parEdge);
				child = parent;
			}
			Collections.reverse(this.shortestPath);
			this.setSP();
		}else if(!this.closeSet.containsKey(sink) && this.openSet.size() <= 1){
			//Cannot find a path from source to sink and Astar ends after openSet becomes empty
			this.shortestLength = Double.MAX_VALUE;
			this.shortestPath = null;
			this.setSP();
		}else{
			Tool.log("Bug in Astar! Ends while openSet is not empty and sink not found.");
		}
	}
	
	private NodeAttribute getMinFInOpen(){
		NodeAttribute minNA = this.openSet.get(1);
		return minNA;
	}
	
	private void removeOpenSet(){
		this.swap(1, this.openSet.size()-1);
		this.openNodeAttribute.remove(this.openSet.get(this.openSet.size()-1).node);
		this.openSet.remove(this.openSet.size()-1);		
		int parPos = 1;
		while(2*parPos < this.openSet.size()){
			int sonPos1 = 2*parPos;
			int sonPos2 = sonPos1 + 1;
			double parF = this.openSet.get(parPos).attribute.f_value;
			double sonF1 = this.openSet.get(sonPos1).attribute.f_value;
			double sonF2 = Double.MAX_VALUE;
			if(sonPos2 < this.openSet.size()){
				// son1 and son2 are in openSet
				sonF2 = this.openSet.get(sonPos2).attribute.f_value;
				double minF = (sonF1 < sonF2)? sonF1:sonF2;
				double maxF = (sonF1 < sonF2)? sonF2:sonF1;
				if(parF > maxF){
					// Parent is larger than both sons. Swap parent with lower son.
					if(minF == sonF1){
						this.swap(parPos, sonPos1);
						parPos = sonPos1;
					}else{
						this.swap(parPos, sonPos2);
						parPos = sonPos2;
					}
				}else if(parF <= maxF && parF > minF){
					// Parent is larger than only one son. Swap parent with that son.
					if(minF == sonF1){
						this.swap(parPos, sonPos1);
						parPos = sonPos1;
					}else{
						this.swap(parPos, sonPos2);
						parPos = sonPos2;
					}
				}else{
					// Parent is less than both son. Keep unchanged.
					break;
				}
			}else{
				// son1 is in openSet, but son2 is out of openSet. Consider son1 only.
				if(parF > sonF1){
					// Parent is larger than son1. Swap them.
					this.swap(parPos, sonPos1);
					parPos = sonPos1;
				}else{
					// Parent is less than son1. Keep unchanged.
					break;
				}
			}
		}
	}
	
	private void addOpenSet(Node node, Attribute attribute){
		NodeAttribute node_attribute = new NodeAttribute(node, attribute);
		this.openSet.add(node_attribute);
		this.openNodeAttribute.put(node, attribute);
		int sonPos = this.openSet.size() - 1;
		while(((int) sonPos/2) > 0){
			// Parent is in openSet
			int parPos = ((int) sonPos/2);
			double parF = this.openSet.get(parPos).attribute.f_value;
			double sonF = this.openSet.get(sonPos).attribute.f_value;
			if(parF > sonF){
				// Parent is larger than son. Swap them.
				this.swap(parPos, sonPos);
				sonPos = parPos;
			}else{
				// Parent is smaller than son. Keep unchanged.
				break;
			}
		}
	}
	
	private boolean openSetContains(Node node){
		if(this.openNodeAttribute.containsKey(node)){
			return true;
		}
		return false;
	}
	
	private Attribute openSetGet(Node node){
		return this.openNodeAttribute.get(node);
	}
	
	private void swap(int pos1, int pos2){
		Node node1 = this.openSet.get(pos1).node;
		Node node2 = this.openSet.get(pos2).node;
		Attribute attr1 = this.openSet.get(pos1).attribute;
		Attribute attr2 = this.openSet.get(pos2).attribute;
		this.openSet.get(pos1).node = node2;
		this.openSet.get(pos2).node = node1;
		this.openSet.get(pos1).attribute = attr2;
		this.openSet.get(pos2).attribute = attr1;
	}
	
	private void setSP(){
		this.SP = new ShortestPath(this.shortestLength, this.shortestPath);
	}
	
	public ShortestPath getSP(){
		return this.SP;
	}
}
