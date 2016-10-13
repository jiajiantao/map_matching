package stmatching;

import stmatching.Time;
import stmatching.Candidate;
import stmatching.Point;
import stmatching.CandidatePreparation;
import stmatching.Dijkstra;
import map.Edge;
import map.Node.NodeId;
import map.Map;
import tool.Tool;

import java.util.*;

public class STAnalysis {
	
	public class CandidatePair{
		public String candidateIdStart;
		public String candidateIdEnd;
		
		public CandidatePair(String startId, String endId){
			this.candidateIdStart = startId;
			this.candidateIdEnd = endId;
		}
		
		@Override
		public boolean equals(Object candPair){
			if(!(candPair instanceof CandidatePair)) return false;
			if(this.candidateIdStart.equals(((CandidatePair) candPair).candidateIdStart)
					&& this.candidateIdEnd.equals(((CandidatePair) candPair).candidateIdEnd)) return true;
			return false;
		}
		
		@Override
		public int hashCode(){
			return this.candidateIdStart.hashCode() + this.candidateIdEnd.hashCode();
		}
		
		@Override
		public String toString(){
			return this.candidateIdStart + "," + this.candidateIdEnd;
		}
	}
	
	private double mu;
	private double sigma;
	private HashMap<String, Double> observationProbabilities;	//key: candidateId
	private HashMap<CandidatePair, Double> transmissionProbabilities;
	private HashMap<CandidatePair, Double> spatialWeights;
	private HashMap<CandidatePair, Double> temporalWeights;
	private HashMap<CandidatePair, Double> candidateGraph;
	private HashMap<CandidatePair, ArrayList<Edge>> shortestPath;
	private HashMap<CandidatePair, Double> shortestLength;
	private static final double MAXDOUBLE = Double.MAX_VALUE;
	
	public STAnalysis(){
		
	}

	public STAnalysis(Map map, CandidatePreparation cp, double mu, double sigma, boolean runRule1, boolean runRule2, boolean runRule3){
		this.setMu(mu);
		this.setSigma(sigma);
		this.setObservationProbabilities(cp, runRule1);
		this.setTransmissionProbabilities(map, cp, runRule2);
		this.setSpatialWeights(cp);
		this.setTemporalWeights(map, cp, runRule3);
		this.setCandidateGraph(cp);
	}
	
	private void setObservationProbabilities(CandidatePreparation cp, boolean runRule1){
		this.observationProbabilities = new HashMap<String, Double>();
		HashMap<Time, ArrayList<Candidate>> candidates = cp.getCandidates();
		for(Time time: candidates.keySet()){
			ArrayList<Candidate> candList = candidates.get(time);
			for(int index = 0; index < candList.size(); index++){
				Candidate candidate = candList.get(index);
				double obsProb = 1;
				if(runRule1) obsProb = this.runObservationProbabilityFunction(candidate);
				this.observationProbabilities.put(candidate.candidateId, obsProb);
			}
		}
	}
	
	private void setTransmissionProbabilities(Map map, CandidatePreparation cp, boolean runRule2){
		this.transmissionProbabilities = new HashMap<CandidatePair, Double>();
		this.shortestLength = new HashMap<CandidatePair, Double>();
		this.shortestPath = new HashMap<CandidatePair, ArrayList<Edge>>();
		ArrayList<Time> times = cp.getTimes();
		HashMap<Time, Point> points = cp.getPoints();
		HashMap<Time, ArrayList<Candidate>> candidates = cp.getCandidates();
		Dijkstra dijkstra = new Dijkstra(map, cp);
		for(int index = 1; index < times.size(); index++){
			Time tNow = times.get(index);
			Time tPre = times.get(index - 1);
			ArrayList<Candidate> candListNow = candidates.get(tNow);
			ArrayList<Candidate> candListPre = candidates.get(tPre);
			for(int iNow = 0; iNow < candListNow.size(); iNow++){
				for(int iPre = 0; iPre < candListPre.size(); iPre++){
					String candIdStart = candListPre.get(iPre).candidateId;
					String candIdEnd = candListNow.get(iNow).candidateId;
					CandidatePair candPair = new CandidatePair(candIdStart, candIdEnd);
					double traProb = 1;
					if(runRule2){
						Point pNow = points.get(tNow);
						Point pPre = points.get(tPre);
						Candidate cNow = candListNow.get(iNow);
						Candidate cPre = candListPre.get(iPre);
						traProb = runTransmissionProbabilityFunction(dijkstra, map, pNow, pPre, cNow, cPre);
					}
					this.transmissionProbabilities.put(candPair, traProb);
				}
			}
		}
	}
	
	private void setSpatialWeights(CandidatePreparation cp){
		this.spatialWeights = new HashMap<CandidatePair, Double>();
		ArrayList<Time> times = cp.getTimes();
		HashMap<Time, ArrayList<Candidate>> candidates = cp.getCandidates();
		for(int index = 1; index < times.size(); index++){
			Time tNow = times.get(index);
			Time tPre = times.get(index - 1);
			ArrayList<Candidate> candListNow = candidates.get(tNow);
			ArrayList<Candidate> candListPre = candidates.get(tPre);
			for(int iNow = 0; iNow < candListNow.size(); iNow++){
				String candIdEnd = candListNow.get(iNow).candidateId;
				double obsProb = this.observationProbabilities.get(candIdEnd);
				for(int iPre = 0; iPre < candListPre.size(); iPre++){
					String candIdStart = candListPre.get(iPre).candidateId;
					CandidatePair candPair = new CandidatePair(candIdStart, candIdEnd);
					double traProb = this.transmissionProbabilities.get(candPair);
					double spatialWeight = obsProb * traProb;
					this.spatialWeights.put(candPair, spatialWeight);
				}
			}
		}
	}
	
	private void setTemporalWeights(Map map, CandidatePreparation cp, boolean runRule3){
		this.temporalWeights = new HashMap<CandidatePair, Double>();
		ArrayList<Time> times = cp.getTimes();
		HashMap<Time, ArrayList<Candidate>> candidates = cp.getCandidates();
		for(int index = 1; index < times.size(); index++){
			Time tNow = times.get(index);
			Time tPre = times.get(index - 1);
			ArrayList<Candidate> candListNow = candidates.get(tNow);
			ArrayList<Candidate> candListPre = candidates.get(tPre);
			for(int iNow = 0; iNow < candListNow.size(); iNow++){
				for(int iPre = 0; iPre < candListPre.size(); iPre++){
					Candidate cPre = candListPre.get(iPre);
					Candidate cNow = candListNow.get(iNow);
					CandidatePair candPair = new CandidatePair(cPre.candidateId, cNow.candidateId);
					double temWgt = 1;
					if(runRule3) temWgt = runTemporalFunction(map, cPre, cNow);
					this.temporalWeights.put(candPair, temWgt);
				}
			}
		}
	}
	
	private void setCandidateGraph(CandidatePreparation cp){
		this.candidateGraph = new HashMap<CandidatePair, Double>();
		ArrayList<Time> times = cp.getTimes();
		HashMap<Time, ArrayList<Candidate>> candidates = cp.getCandidates();
		for(int index = 1; index < times.size(); index++){
			Time tNow = times.get(index);
			Time tPre = times.get(index - 1);
			ArrayList<Candidate> candListNow = candidates.get(tNow);
			ArrayList<Candidate> candListPre = candidates.get(tPre);
			for(int iNow = 0; iNow < candListNow.size(); iNow++){
				for(int iPre = 0; iPre < candListPre.size(); iPre++){
					String candIdStart = candListPre.get(iPre).candidateId;
					String candIdEnd = candListNow.get(iNow).candidateId;
					CandidatePair candPair = new CandidatePair(candIdStart, candIdEnd);
					double spatialWeight = this.spatialWeights.get(candPair);
					double temporalWeight = this.temporalWeights.get(candPair);
					this.candidateGraph.put(candPair, spatialWeight*temporalWeight);
				}
			}
		}
	}
	
	private double runObservationProbabilityFunction(Candidate candidate){
		double x = candidate.distance;
		return 1/(Math.sqrt(2*Math.PI)*this.sigma) * Math.pow(Math.E, -Math.pow(x-this.mu, 2)/(2*Math.pow(this.sigma, 2)));	
	}
	
	private double runTransmissionProbabilityFunction(Dijkstra dijkstra, Map map, Point pNow, Point pPre, Candidate cNow, Candidate cPre){
		double traProb = -1;
		double minLength = MAXDOUBLE;
		ArrayList<Edge> minPath = null;
		double pointDistance = Tool.getDistance(pNow.lat, pNow.lon, pPre.lat, pPre.lon);	
		for(Edge edgePre: cPre.edgeAlpha.keySet()){
			NodeId sourceId = edgePre.nodeEnd.nodeId;
			double lenToSource = cPre.edgeAlpha.get(edgePre) * edgePre.length;
			for(Edge edgeNow: cNow.edgeAlpha.keySet()){
				double length = MAXDOUBLE;
				ArrayList<Edge> path = new ArrayList<Edge>();
				if(cNow.lon == cPre.lon && cNow.lat == cPre.lat){ //share the same candidate
					length = 0;
					path = null;
				}else if(edgeNow.equals(edgePre) && cPre.edgeAlpha.get(edgePre) >= cNow.edgeAlpha.get(edgeNow)){
					length = (cPre.edgeAlpha.get(edgePre) - cNow.edgeAlpha.get(edgeNow)) * edgePre.length;
					path.add(edgeNow);
				}else{
					NodeId sinkId = edgeNow.nodeStart.nodeId;
					double lenFromSink = (1 - cNow.edgeAlpha.get(edgeNow)) * edgeNow.length;
					double stDistance = dijkstra.getShortestLength(sourceId, sinkId);
					length = lenToSource + stDistance + lenFromSink;
					ArrayList<Edge> spath = dijkstra.getShortestPath(sourceId, sinkId);
					if(spath == null) path = null;
					else{
						path.add(edgePre);
						path.addAll(spath);
						path.add(edgeNow);
					}
				}
				if(length < minLength){
					minLength = length;
					minPath = path;
				}
			}
		}
		this.shortestPath.put(new CandidatePair(cPre.candidateId, cNow.candidateId), minPath);
		this.shortestLength.put(new CandidatePair(cPre.candidateId, cNow.candidateId), minLength);
		if(minLength == 0) return 0;
		traProb = pointDistance / minLength;
		return traProb;
	}
	
	private double runTemporalFunction(Map map, Candidate cPre, Candidate cNow){
		double temProb = -1;
		CandidatePair candPair = new CandidatePair(cPre.candidateId, cNow.candidateId);
		if(this.shortestPath.get(candPair) == null) return 0;	//not reachable
		if(this.shortestPath.get(candPair).isEmpty()) return 0;	//no edge
		if(this.shortestLength.get(candPair) == 0) return 0;	//no move
		ArrayList<Edge> path = this.shortestPath.get(candPair);
		double length = this.shortestLength.get(candPair);					//unit m
		double deltaT = Math.abs(cNow.time.epoch - cPre.time.epoch)/1000; 	//unit s
		double vMean = (length/deltaT) * 3.6;	//unit km/h
		double sumEM = 0;
		double sumE2 = 0;
		double sumM2 = 0;
		for(int index = 0; index < path.size(); index++){
			if(path.get(index) == null) return 0;
			Edge edge = path.get(index);
			double vEdge = edge.v; 	//unit km/h
			sumEM += vEdge * vMean;
			sumE2 += Math.pow(vEdge, 2);
			sumM2 += Math.pow(vMean, 2);
		}
		temProb = sumEM / (Math.sqrt(sumE2) * Math.sqrt(sumM2));
		return temProb;
	}
	
	private void setMu(double mu){
		this.mu = mu;
	}
	
	private void setSigma(double sigma){
		this.sigma = sigma;
	}

	public HashMap<CandidatePair, Double> getCandidateGraph(){
		return this.candidateGraph;
	}
	
	public HashMap<String, Double> getObservationProbabilities(){
		return this.observationProbabilities;
	}
	
	public HashMap<CandidatePair, Double> getTransmissionProbabilities(){
		return this.transmissionProbabilities;
	}
	
	public HashMap<CandidatePair, Double> getSpatialWeights(){
		return this.spatialWeights;
	}
	
	public HashMap<CandidatePair, Double> getTemporalWeights(){
		return this.temporalWeights;
	}
}
