package stmatching;

import stmatching.STAnalysis.CandidatePair;
import stmatching.Time;

import java.util.*;

public class ResultMatching {
	private HashMap<Time, Candidate> candidatesMatched;
	private HashMap<String, String> parents;	//candidateId(key) parentId(value)
	private HashMap<String, Double> maxLengths;//candidateId(key) 
	private static final double MINDOUBLE = Double.MIN_VALUE;
	
	public ResultMatching(STAnalysis sta, CandidatePreparation cp){
		this.initializeMaxLengths(sta, cp);
		this.initializeParents();
		this.matchSequence(sta, cp);
		this.setCandidatesMatched(cp);
	}
	
	private void initializeMaxLengths(STAnalysis sta, CandidatePreparation cp){
		this.maxLengths = new HashMap<String, Double>();
		HashMap<String, Double> obsProbs = sta.getObservationProbabilities();
		Time t0 = cp.getTimes().get(0);
		ArrayList<Candidate> candList0 = cp.getCandidates().get(t0);
		for(int index = 0; index < candList0.size(); index++){
			String candId = candList0.get(index).candidateId;
			double obsProb = obsProbs.get(candId);
			this.maxLengths.put(candId, obsProb);
		}
	}
	
	private void initializeParents(){
		this.parents = new HashMap<String, String>();
	}
	
	private void matchSequence(STAnalysis sta, CandidatePreparation cp){
		HashMap<CandidatePair, Double> candidateGraph = sta.getCandidateGraph();
		ArrayList<Time> times = cp.getTimes();
		HashMap<Time, ArrayList<Candidate>> candidates = cp.getCandidates();
		for(int index = 1; index < times.size(); index++){
			Time tNow = times.get(index);
			Time tPre = times.get(index - 1);
			ArrayList<Candidate> candListNow = candidates.get(tNow);
			ArrayList<Candidate> candListPre = candidates.get(tPre);
			for(int iNow = 0; iNow < candListNow.size(); iNow++){
				String candIdEnd = candListNow.get(iNow).candidateId;
				double maxLength = MINDOUBLE;
				String parent = "-1";
				for(int iPre = 0; iPre < candListPre.size(); iPre++){
					String candIdStart = candListPre.get(iPre).candidateId;
					CandidatePair candPair = sta.new CandidatePair(candIdStart, candIdEnd);
					double weight = candidateGraph.get(candPair);
					double length = this.maxLengths.get(candIdStart) + weight;
					if(length <= maxLength) continue;
					maxLength = length;
					parent = candIdStart;
				}
				this.maxLengths.put(candIdEnd, maxLength);
				this.parents.put(candIdEnd, parent);
			}
		}
	}
	
	private void setCandidatesMatched(CandidatePreparation cp){
		this.candidatesMatched = new HashMap<Time, Candidate>();
		HashMap<Time, ArrayList<Candidate>> candidates = cp.getCandidates();
		HashMap<String, Candidate> candidatesMap = new HashMap<String, Candidate>();
		for(Time time: candidates.keySet()){
			ArrayList<Candidate> candList = candidates.get(time);
			for(int index = 0; index < candList.size(); index++){
				Candidate candidate = candList.get(index);
				candidatesMap.put(candidate.candidateId, candidate);
			}
		}

		String lastMaxCandId = this.findLastMaxCandidateId();
		String child = lastMaxCandId;
		Candidate candidate = candidatesMap.get(child);
		this.candidatesMatched.put(candidate.time, candidate);
		while(this.parents.containsKey(child)){
			String parent = this.parents.get(child);
			Candidate parCand = candidatesMap.get(parent);
			this.candidatesMatched.put(parCand.time, parCand);
			child = parent;
		}
	}
	
	private String findLastMaxCandidateId(){
		double maxLength = MINDOUBLE;
		String maxCandId = "-1";
		for(String cid: this.maxLengths.keySet()){
			double length = this.maxLengths.get(cid);
			if(length <= maxLength) continue;
			maxLength = length;
			maxCandId = cid;
		}
		return maxCandId;
	}
	
	public HashMap<Time, Candidate> getSequenceMatched(){
		return this.candidatesMatched;
	}
}
