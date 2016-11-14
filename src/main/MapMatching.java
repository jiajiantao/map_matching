package main;

import stmatching.CandidatePreparation;
import stmatching.ResultMatching;
import stmatching.STAnalysis;
import stmatching.Candidate;
import map.Map;
import output.Output;
import tool.Tool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class MapMatching {

	public static void main(String args[]) throws IOException, InterruptedException {
		Tool.log("Start...");
		// **************** Generate Map *****************************
		//String mapFile = "./shenzhen2013-shifted-formated.osm";
		String mapFile = "./hangzhou.osm";
		
		String edgesOut = "./data/bus_point_candidate/edges.txt";
		Map map = new Map(mapFile);
		Output.outputEdgesTxt(edgesOut, map);
		Tool.log("Done: map generation.\n---------------------------------");

		String[] routes = {
				/* test routes */
				//"B6364_2", "B6374_1", "B6374_2",
				// "B6384_1", "B6384_2",
				// "M2163_1", "M2173_1"
		};
		extractSubroutes(routes); // call bash to extract subroutes

		for (String route : routes) {
			// path of passes
			String path = "./data/bus_gps/R" + route + "/";
			// read # of passes
			BufferedReader br = new BufferedReader(new FileReader(path + "R" + route + ".count"));
			int n = Integer.parseInt(br.readLine());
			br.close();

			// ****************** st-matching **********************
			// get point-candidate pairs in each pass
			for (int i = 1; i <= n; i += 1) {
				Tool.log(String.format("Start: route %s_%s st-matching.", route, i));
				CandidatePreparation cp = getCandidatePreparation(map, route, 0, 0, i);
				st_matching(map, route, 0, cp, 0, i);
				// String candfile =
				// String.format("./data/bus_point_candidate/bus_candidateMatched_R%s_%s_%s_%s.OSM",
				// route, i, pointGap, bias);
				Tool.log(String.format("Done: %s_%s st-matching!\n---------------------------------", route, i));
			}
		}
		Tool.log("All done!");
	}

	public static void st_matching(Map map, String route, int pointGap, CandidatePreparation cp, int bias, int i)
			throws IOException {
		// **************** spatial and temporal analysis ************
		double mu = 0;
		double sigma = 20;
		STAnalysis sta = new STAnalysis(map, cp, mu, sigma, true, true, true);
		Tool.log("Done: spatial-temporal analysis.");
		// **************** result matching **************************
		String candidatesMatchedOsm = String.format("./data/bus_point_candidate/bus_candidateMatched_R%s_%s_%s_%s.OSM",
				route, i, pointGap, bias);
		ResultMatching rm = new ResultMatching(sta, cp);
		Output.outputCandidatesMatchedOsm(candidatesMatchedOsm, rm, cp.getTimes());
		Tool.log("Done: result matching.");
	}

	public static CandidatePreparation getCandidatePreparation(Map map, String route, int pointGap, int bias, int i)
			throws IOException {
		String gpsFile = String.format("./data/bus_gps/R%s/R%s_%s.txt", route, route, i);
		String candidatesOutOsm = String.format("./data/bus_point_candidate/bus_candidate_R%s_%s_%s_%s.OSM", route, i,
				pointGap, bias);
		String pointsOutOsm = String.format("./data/bus_point_candidate/bus_point_R%s_%s_%s_%s.OSM", route, i, pointGap,
				bias);
		int searchRadius = 100;
		int maxCandidateNo = 5;
		CandidatePreparation cp = new CandidatePreparation(gpsFile, map, searchRadius, maxCandidateNo, pointGap, bias);
		Output.outputCandidatesOsm(candidatesOutOsm, cp);
		Output.outputPointsOsm(pointsOutOsm, cp);
		Tool.log(String.format("Done: candidation preparation.", route, i));
		return cp;
	}

	public static int getIndexOf(ArrayList<Candidate> cands, Candidate cand) {
		if (cand == null)
			return -1;
		for (int i = 0; i < cands.size(); i++) {
			if (cands.get(i).lon == cand.lon && cands.get(i).lat == cand.lat) {
				return i;
			}
		}
		return -1;
	}

	public static void extractSubroutes(String[] routes) throws InterruptedException, IOException {
		HashSet<String> routeSet = new HashSet<String>();
		for (String r : routes)
			routeSet.add(r.split("_")[0]);
		StringBuilder sb = new StringBuilder();
		for (String r : routeSet)
			sb.append(r + " ");
		String[] cmd = { "sh", "-c", "cd ./data/bus_gps; bash run_extractor.sh \"" + sb.toString() + "\"" };
		Tool.callShell(cmd);
	}
}
