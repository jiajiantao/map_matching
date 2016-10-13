package output;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import stmatching.Candidate;
import stmatching.CandidatePreparation;
import stmatching.Point;
import stmatching.ResultMatching;
import stmatching.Time;
import map.Map;
import map.Edge;

public class Output {
	private static String head = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> \n"
			+ "<osm version=\"0.6\" generator=\"Overpass API\"> \n"
			+ "<note>The data included in this document is from www.openstreetmap.org. The data is made available under ODbL.</note> \n"
			+ "<meta osm_base=\"2014-11-18T02:26:02Z\"/> \n"
			+ "\n"
			+ "<bounds minlat=\"22.439\" minlon=\"113.725\" maxlat=\"22.8182\" maxlon=\"114.359\"/> \n"
			+ "\n";
	private static String tail = "</osm>";

	public static void outputEdgesTxt(String outFile, Map map) throws IOException{
		//output edges
		BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
		for(Edge edge: map.edgeSet){
			bw.write(edge.toString());
			bw.newLine();
		}
		bw.flush();
		bw.close();	
	}
	
	public static void outputCandidatesTxt(String outFile, CandidatePreparation cp) throws IOException{
		//output candidates
		ArrayList<Time> times = cp.getTimes();
		HashMap<Time, ArrayList<Candidate>> candidates = cp.getCandidates();
		BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
		for(int index = 0; index < times.size(); index++){
			Time time = times.get(index);
			if(!candidates.containsKey(time)) continue;
			ArrayList<Candidate> candList = candidates.get(time);
			for(int i = 0; i < candList.size(); i++){
				bw.write(candList.get(i).toString());
				bw.newLine();
			}
		}
		bw.flush();
		bw.close();			
	}
	
	public static void outputCandidatesOsm(String outFile, CandidatePreparation cp) throws IOException{
		//ouput candidates in OSM format for visualization
		ArrayList<Time> times = cp.getTimes();
		HashMap<Time, ArrayList<Candidate>> candidates = cp.getCandidates();
		BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
		bw.write(head);
		for(int index = 0; index < times.size(); index++){
			Time time = times.get(index);
			if(!candidates.containsKey(time)) continue;
			ArrayList<Candidate> candList = candidates.get(time);
			for(int i = 0; i < candList.size(); i++){
				String output = "<node id=\"" + candList.get(i).candidateId + "\" lat=\"" + candList.get(i).lat + "\" lon=\"" + candList.get(i).lon + "\" version=\"3\" timestamp=\"2013-09-21T03:53:35Z\" changeset=\"17949996\" uid=\"44514\" user=\"Huawei\">" + "\n" 
						+ "\t<tag k=\"name\" v= \"candidate" + candList.get(i) + "\"/>\n"
						+ "\t<tag k=\"name:fr\" v= \"candidate\"/>\n"
						+ "\t<tag k=\"natural\" v=\"peak\"/>\n"
						+ "  </node>\n";
				bw.write(output);
			}
		}
		bw.write(tail);
		bw.flush();
		bw.close();		
	}
	
	public static void outputPathOsm(String outFile, ArrayList<Edge> edgeList) throws IOException{
		//ouput candidates in OSM format for visualization
		BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
		bw.write(head);
		for(int index = 0; index < edgeList.size(); index++){
			Edge edge = edgeList.get(index);
			String output = "<node id=\"" + index+"1" + "\" lat=\"" + edge.nodeStart.lat + "\" lon=\"" + edge.nodeStart.lon + "\" version=\"3\" timestamp=\"2013-09-21T03:53:35Z\" changeset=\"17949996\" uid=\"44514\" user=\"Huawei\">" + "\n" 
					+ "\t<tag k=\"name\" v= \"node" + edge.nodeStart + "\"/>\n"
					+ "\t<tag k=\"name:fr\" v= \"node\"/>\n"
					+ "\t<tag k=\"natural\" v=\"peak\"/>\n"
					+ "  </node>\n"
					+ "<node id=\"" + index+"2" + "\" lat=\"" + edge.nodeEnd.lat + "\" lon=\"" + edge.nodeEnd.lon + "\" version=\"3\" timestamp=\"2013-09-21T03:53:35Z\" changeset=\"17949996\" uid=\"44514\" user=\"Huawei\">" + "\n" 
					+ "\t<tag k=\"name\" v= \"node" + edge.nodeEnd + "\"/>\n"
					+ "\t<tag k=\"name:fr\" v= \"node\"/>\n"
					+ "\t<tag k=\"natural\" v=\"peak\"/>\n"
					+ "  </node>\n";
			bw.write(output);
		}
		bw.write(tail);
		bw.flush();
		bw.close();		
	}
	
	public static void outputPointsOsm(String outFile, CandidatePreparation cp) throws IOException{
		ArrayList<Time> times = cp.getTimes();
		HashMap<Time, Point> points = cp.getPoints();
		BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
		bw.write(head);
		for(int index = 0; index < times.size(); index++){
			Time time = times.get(index);
			Point point = points.get(time);
			String output = "<node id=\"" + time.epoch + "\" lat=\"" + point.lat + "\" lon=\"" + point.lon + "\" version=\"3\" timestamp=\"2013-09-21T03:53:35Z\" changeset=\"17949996\" uid=\"44514\" user=\"Huawei\">" + "\n" 
					+ "\t<tag k=\"name\" v= \"point" + point + "\"/>\n"
					+ "\t<tag k=\"name:fr\" v= \"point\"/>\n"
					+ "\t<tag k=\"natural\" v=\"peak\"/>\n"
					+ "  </node>\n";
			bw.write(output);
		}		
		bw.write(tail);
		bw.flush();
		bw.close();	
	}
	
	public static void outputCandidatesMatchedOsm(String outFile, ResultMatching rm, ArrayList<Time> times) throws IOException{
		HashMap<Time, Candidate> candidatesMatched = rm.getSequenceMatched();
		BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
		bw.write(head);
		for(int index = 0; index < times.size(); index++){
			Time time = times.get(index);
			Candidate candidate = candidatesMatched.get(time);
			String output = "<node id=\"" + candidate.candidateId + "\" lat=\"" + candidate.lat + "\" lon=\"" + candidate.lon + "\" version=\"3\" timestamp=\"2013-09-21T03:53:35Z\" changeset=\"17949996\" uid=\"44514\" user=\"Huawei\">" + "\n" 
					+ "\t<tag k=\"name\" v= \"candidate" + candidate + "\"/>\n"
					+ "\t<tag k=\"name:fr\" v= \"candidate\"/>\n"
					+ "\t<tag k=\"natural\" v=\"peak\"/>\n"
					+ "  </node>\n";
			bw.write(output);
		}
		bw.write(tail);
		bw.flush();
		bw.close();	
	}
	
	public static void outputLine(String sOut, BufferedWriter bw) throws IOException{
		bw.write(sOut);
		bw.newLine();
	}
	
}
