package evaluation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tool.Tool;

public class Evaluation {
	public static HashSet<String> getTrueRoute(String routefile, String edgefile) throws IOException, InterruptedException {
		// store all ground truth nodes
		HashSet<String> nodeSet = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(routefile));
		String sBuf = null;
		while((sBuf = br.readLine()) != null) {
			if(!sBuf.contains("SimpleData name=\"id\""))
				continue;
			Matcher m = Pattern.compile("<SimpleData name=\"id\">(.*?)</SimpleData>").matcher(sBuf);
			while(m.find()) {
				String nid = m.group(1);
				nodeSet.add(nid);
			}
		}
		br.close();
		// add edge
		HashSet<String> route = new HashSet<String>();
		br = new BufferedReader(new FileReader(routefile));
		sBuf = null;
		while((sBuf = br.readLine()) != null) {
			if(!sBuf.contains("SimpleData name=\"id\""))
				continue;
			Matcher m = Pattern.compile("<SimpleData name=\"id\">(.*?)</SimpleData>").matcher(sBuf);
			while(m.find()) {
				String nid = m.group(1);
				String[] cmd = {"sh", "-c", String.format("grep ',%s,' %s", nid, edgefile)};
				Process process = Tool.callShell(cmd);
				BufferedReader inr = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String ins = null;
				while((ins = inr.readLine()) != null) {
					String edge = ins.split(",")[0];
					String nStart = ins.split(",")[1];
					String nEnd = ins.split(",")[2];
					if(nodeSet.contains(nStart) && nodeSet.contains(nEnd))
						route.add(edge);
				}
				process.waitFor();
				inr.close();
			}
		}
		br.close();
		return route;
	}
	
	public static double getAccuracy(String candfile, HashSet<String> route) throws IOException {
		double n = 0;	// # of candidates
		double p = 0;	// # of positive cases
		BufferedReader br = new BufferedReader(new FileReader(candfile));
		String sBuf = null;
		while((sBuf = br.readLine()) != null) {
			if(!sBuf.contains("<tag k=\"name\" v= \"candidate"))
				continue;
			// find a candidate
			n++;
			String[] ss = sBuf.split(",");
			// find any edge the candidate is in. check if route contains the edge.
			for(int i = 4; i < ss.length; i += 2) {
				String edge = ss[i];
				if(route.contains(edge)) {
					// found positive case
					p++;
					break;
				}
			}
		}
		br.close();
		return p/n;
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
//		String routefile = "./route_labeled/B6364_2_extra50-done/route_labeled.kml";
//		String edgefile = "./data/bus_point_candidate/edges.txt";
//		HashSet<String> route = getTrueRoute(routefile, edgefile);
//		
//		String candfile = "./data/bus_point_candidate/bus_candidateMatched_RB6364_2_9.OSM";
//		double acc = getAccuracy(candfile, route);
	}

}
