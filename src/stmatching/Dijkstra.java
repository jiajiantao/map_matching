package stmatching;

import java.util.*;

import stmatching.CandidatePreparation;
import map.Map;
import map.Edge;
import map.Node;
import map.Node.NodeId;
import map.Node.NodePair;
import tool.Tool;

public class Dijkstra {
	private HashMap<NodePair, Double> basicMap;
	private HashMap<NodePair, Edge> npairEdges;
	private HashMap<NodeId, HashSet<NodeId>> neighborSets;
	private HashMap<NodeId, HashSet<NodeId>> neighborFromSets;
	private HashSet<NodeId> sourceSet;
	private ArrayList<NodeId> sinkSet;	//set of sinks from one source
	private double MAXLON;
	private double MINLON;
	private double MAXLAT;
	private double MINLAT;
	private boolean doneGetShortestLength = false;
	
	private HashMap<NodeId, Double> distanceMatrix;
	private HashMap<NodePair, Double> shortestLength;
	private ArrayList<Edge> shortestPath;
	private static final double MAXDOUBLE = Double.MAX_VALUE;
	
	public Dijkstra(Map map, CandidatePreparation cp){
		this.setRange(cp);
		this.setBasicMap(map);
		this.setNpairEdges(map);
		this.setSourceSet();
		this.setNeighborSets();
		this.setNeighborFromSets();
		this.initializeShortestLength();
	}
	
	public double getShortestLength(NodeId sourceNid, NodeId sinkNid) {
		this.initializeShortestLength();
		this.setSinkSet(sourceNid);
		if(!this.sinkSet.contains(sinkNid)) return MAXDOUBLE;
		if(sourceNid.equals(sinkNid)) return 0;
		this.initializeDistanceMatrix();
		NodePair stPair = (new Node()).new NodePair(sourceNid, sinkNid);
		while(!this.shortestLength.containsKey(stPair)){
			HashMap<NodeId,Double> minNidDistance = this.findMinDistance();
			this.putMinToShortestMap(sourceNid,minNidDistance);
			this.renewDistanceMatrix(minNidDistance,sourceNid);
		}
		this.doneGetShortestLength = true;
		return this.shortestLength.get(stPair);	
	}
	
	public ArrayList<Edge> getShortestPath(NodeId sourceNid, NodeId sinkNid){
		//must be called after getShortestLength()
		if(!this.doneGetShortestLength){
			Tool.log("Error! Run getShortestLength() first.\n getShortestPath must be called after getShortestLength() is done!");
			return null;
		}
		if(!this.sinkSet.contains(sinkNid)) return null;
		if(sourceNid.equals(sinkNid)) return new ArrayList<Edge>();
		this.shortestPath = new ArrayList<Edge>();
		NodeId child = sinkNid;
		NodeId parent = sinkNid;
		double scLength = this.shortestLength.get((new Node()).new NodePair(sourceNid, sinkNid));
		while(!parent.equals(sourceNid)){
			HashSet<NodeId> neighborsFrom = this.neighborFromSets.get(child);
			for(Iterator<NodeId> it = neighborsFrom.iterator(); it.hasNext();){
				parent = it.next();
				NodePair pcPair = (new Node()).new NodePair(parent, child);
				NodePair spPair = (new Node()).new NodePair(sourceNid, parent);
				if(parent.equals(sourceNid) && this.shortestLength.containsKey(pcPair)){
					this.shortestPath.add(this.npairEdges.get(pcPair));
					return this.shortestPath;
				}
				if(!this.shortestLength.containsKey(spPair)) continue;
				double pcLength = this.npairEdges.get(pcPair).length;
				double spLength = this.shortestLength.get(spPair);
				if((spLength + pcLength) == scLength || parent.equals(sourceNid)){
					this.shortestPath.add(this.npairEdges.get(pcPair));
					child = parent;
					scLength = spLength;
					break;
				}
			}
		}
		return this.shortestPath;
	}
	
	private void setNeighborFromSets(){
		this.neighborFromSets = new HashMap<NodeId,HashSet<NodeId>>();  
		for(NodePair nodePair: this.basicMap.keySet()){
			NodeId nidStart = nodePair.nodeIdStart;
			NodeId nidEnd = nodePair.nodeIdEnd;
        	if(neighborFromSets.containsKey(nidEnd)){
        		neighborFromSets.get(nidEnd).add(nidStart);
        	}else{
        		HashSet<NodeId> aSet = new HashSet<NodeId>();
        		aSet.add(nidStart);
        		neighborFromSets.put(nidEnd, aSet);
        	}
        }  
	}
	
	private void setBasicMap(Map map){
		basicMap = new HashMap<NodePair, Double>();
		HashSet<Edge> edgeSet = map.edgeSet;
		for(Edge edge: edgeSet){
			if(edge.nodeStart.lon > this.MAXLON && edge.nodeEnd.lon > this.MAXLON) continue;
			if(edge.nodeStart.lon < this.MINLON && edge.nodeEnd.lon < this.MINLON) continue;
			if(edge.nodeStart.lat > this.MAXLAT && edge.nodeEnd.lat > this.MAXLAT) continue;
			if(edge.nodeStart.lat < this.MINLAT && edge.nodeEnd.lat < this.MINLAT) continue;
			NodeId nidStart = edge.nodeStart.nodeId;
			NodeId nidEnd = edge.nodeEnd.nodeId;
			NodePair nodePair = (new Node()).new NodePair(nidStart, nidEnd);
			double length = edge.length;
			this.basicMap.put(nodePair, length);
		}
	}
	
	private void setSourceSet(){
		this.sourceSet = new HashSet<NodeId>();  
		for(NodePair nodePair: this.basicMap.keySet()){
			this.sourceSet.add(nodePair.nodeIdStart);
		}
	}
	
	private void setNeighborSets() {
		this.neighborSets = new HashMap<NodeId,HashSet<NodeId>>();  
		for(NodePair nodePair: this.basicMap.keySet()){
			NodeId nidStart = nodePair.nodeIdStart;
			NodeId nidEnd = nodePair.nodeIdEnd;
        	if(neighborSets.containsKey(nidStart)){
        		neighborSets.get(nidStart).add(nidEnd);
        	}else{
        		HashSet<NodeId> aSet = new HashSet<NodeId>();
        		aSet.add(nidEnd);
        		neighborSets.put(nidStart, aSet);
        	}
        }      
	}
	
	private void setSinkSet(NodeId sourceId){
		sinkSet=new ArrayList<NodeId>();
		sinkSet.add(sourceId);
		recurseSearchNeighborSets(sourceId);
	}
	
	private void recurseSearchNeighborSets(NodeId nid){
		if(neighborSets.containsKey(nid)){
			HashSet<NodeId> neighborSet = neighborSets.get(nid);
			for(Iterator<NodeId> it = neighborSet.iterator(); it.hasNext();){
				NodeId neighborId = it.next();
				if(!sinkSet.contains(neighborId)){
					sinkSet.add(neighborId);
					recurseSearchNeighborSets(neighborId);
				}
			}
		}
	}
	
	private void initializeDistanceMatrix(){
		distanceMatrix = new HashMap<NodeId, Double>();
		double distance = MAXDOUBLE;
		NodeId sourceId = sinkSet.get(0);
		HashSet<NodeId> neighbors = neighborSets.get(sourceId);
		for(int index = 1; index < sinkSet.size(); index++){//ignore source node
			if(neighbors.contains(sinkSet.get(index))){
				NodePair nodePair = (new Node()).new NodePair(sourceId, sinkSet.get(index));
				distance = basicMap.get(nodePair);
			}
			else distance = MAXDOUBLE;
			distanceMatrix.put(sinkSet.get(index), distance);
		}
	}
	
	private HashMap<NodeId,Double> findMinDistance(){
		HashMap<NodeId, Double> minNidDistance = new HashMap<NodeId,Double>();
		NodeId minNid = null;
		double minDistance = MAXDOUBLE;
		for(NodeId nid: distanceMatrix.keySet()){
			if(distanceMatrix.get(nid) < minDistance){
				minNid = nid;
				minDistance = distanceMatrix.get(nid);
			}
		}
		minNidDistance.put(minNid, minDistance);
		return minNidDistance;
	}
	
	private void putMinToShortestMap(NodeId sourceId,HashMap<NodeId,Double> minNidDistance){
		NodeId sinkId = null;
		double minDistance = MAXDOUBLE;
		for(NodeId nid: minNidDistance.keySet()){
			sinkId = nid;
			minDistance = minNidDistance.get(sinkId);
		}
		NodePair nodePair = (new Node()).new NodePair(sourceId, sinkId);
		shortestLength.put(nodePair, minDistance);
	}
	
	private void renewDistanceMatrix(HashMap<NodeId,Double> minNidDistance,NodeId sourceId){
		NodeId minNid = null;
		double minDistance = MAXDOUBLE;
		for(NodeId nid: minNidDistance.keySet()){
			minNid = nid;
			minDistance = minNidDistance.get(nid);
		}
		changeMinToMaxInDistanceMatrix(minNid);
		HashSet<NodeId> neighbors=findMinNidNeighbors(minNid);
		renewNeighborCost(minDistance,minNid,neighbors,sourceId);
	}
	
	private void changeMinToMaxInDistanceMatrix(NodeId nid){
		distanceMatrix.put(nid, MAXDOUBLE);
	}
	
	private HashSet<NodeId> findMinNidNeighbors(NodeId nid){
		if(!neighborSets.containsKey(nid)) return new HashSet<NodeId>();
		return neighborSets.get(nid);
	}
	
	private void renewNeighborCost(double minDistance, NodeId nid, HashSet<NodeId> neighbors, NodeId sourceId){
		if(neighbors.isEmpty()) return;
		for(Iterator<NodeId> it = neighbors.iterator(); it.hasNext();){
			NodeId neighborId = it.next();
			NodePair nodePair = (new Node()).new NodePair(sourceId, neighborId);
			if(!shortestLength.containsKey(nodePair) && !neighborId.equals(sourceId)){
				NodePair neighborPair = (new Node()).new NodePair(nid, neighborId);
				double neighborDistance = basicMap.get(neighborPair);
				double newDistance = minDistance + neighborDistance;
				double oldDistance = distanceMatrix.get(neighborId);
				if(newDistance >= oldDistance) continue;
				distanceMatrix.put(neighborId, newDistance);
			}
		}
	}

	private void setRange(CandidatePreparation cp){
		this.MAXLON = cp.getMAXLON();
		this.MINLON = cp.getMINLON();
		this.MAXLAT = cp.getMAXLAT();
		this.MINLAT = cp.getMINLAT();
	}
	
	private void initializeShortestLength(){
		this.shortestLength = new HashMap<NodePair, Double>();
	}

	private void setNpairEdges(Map map){
		this.npairEdges = new HashMap<NodePair, Edge>();
		for(Edge edge: map.edgeSet){
			NodeId startId = edge.nodeStart.nodeId;
			NodeId endId = edge.nodeEnd.nodeId;
			this.npairEdges.put((new Node()).new NodePair(startId, endId), edge);
		}
	}
}
