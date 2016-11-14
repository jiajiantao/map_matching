package map;

import map.Node;
import map.Edge;
import map.Node.NodeId;
import map.Speed;

import java.util.*;
import java.util.regex.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Map {

	private HashMap<NodeId, Node> nodes; // nodeId(key) node
	public HashSet<Edge> edgeSet;
	public HashMap<Node, ArrayList<Edge>> outEdges;
	public HashMap<Node, ArrayList<Edge>> inEdges;

	public Map() {

	}

	public Map(String mapFile) throws IOException {
		this.setNodes(mapFile); // setNodeSet(mapFile);
		this.setEdges(mapFile); // setEdgeSet(mapFile);
		this.setOutAndInEdges();
	}

	private void setNodes(String mapFile) throws IOException {
		this.nodes = new HashMap<NodeId, Node>();
		BufferedReader br = new BufferedReader(new FileReader(mapFile));
		String sBuf = null;
		String regex = "node id=\"(.*?)\" lat=\"(.*?)\" lon=\"(.*?)\"";
		while ((sBuf = br.readLine()) != null) {
			Matcher m = Pattern.compile(regex).matcher(sBuf);
			if (!m.find())
				continue;
			String nodeId = m.group(1);
			double lon = Double.parseDouble(m.group(3));
			double lat = Double.parseDouble(m.group(2));
			Node node = new Node(nodeId, lon, lat);
			this.nodes.put(node.nodeId, node);
		}
		br.close();
	}

	private void setEdges(String mapFile) throws IOException {
		this.edgeSet = new HashSet<Edge>();
		String way = "";
		boolean wayFlag = false;
		BufferedReader br = new BufferedReader(new FileReader(mapFile));
		String sBuf = null;
		while ((sBuf = br.readLine()) != null) {
			if (sBuf.contains("<way")) {
				wayFlag = true;
				way = "";
			} else if (sBuf.contains("</way>")) {
				way += sBuf;
				wayFlag = false;
				// if(!way.contains("highway")) continue;
				String wayId = null;
				String wayType = null;
				boolean oneWay = false;
				Matcher mWayId = Pattern.compile("<way id=\"(.*?)\"").matcher(way);
				Matcher mWayType = Pattern.compile("<tag k=\"highway\" v=\"(.*?)\"/>").matcher(way);
				Matcher mOneWay = Pattern.compile("<tag k=\"oneway\" v=\"(.*?)\"/>").matcher(way);
				if (mWayId.find())
					wayId = mWayId.group(1);
				if (mWayType.find())
					wayType = mWayType.group(1);
				if (mOneWay.find() && mOneWay.group(1).equals("yes"))
					oneWay = true;
				// if(wayType == null) continue; //not bus-used wayType
				// if(Speed.get(wayType) == -1) continue;
				Matcher mNodeId = Pattern.compile("<nd ref=\"(.*?)\"(.*?)/>").matcher(way);
				int eIndex = 0;
				Node lastNode = null;
				Node nowNode = null;
				NodeId nowId = null;
				while (mNodeId.find()) {
					nowId = new Node().new NodeId(mNodeId.group(1));
					nowNode = this.nodes.get(nowId);
					if (lastNode == null) {
						lastNode = nowNode;
						continue;
					}
					if (oneWay == true) {
						String edgeId = wayId + "_" + (++eIndex);
						Edge edge = new Edge(edgeId, lastNode, nowNode, wayType, oneWay);
						this.edgeSet.add(edge);
					} else {
						String edgeId1 = wayId + "_" + (++eIndex);
						String edgeId2 = wayId + "_" + (++eIndex);
						Edge edge1 = new Edge(edgeId1, lastNode, nowNode, wayType, oneWay);
						Edge edge2 = new Edge(edgeId2, nowNode, lastNode, wayType, oneWay);
						this.edgeSet.add(edge1);
						this.edgeSet.add(edge2);
					}
					lastNode = nowNode;
				}
			}
			if (wayFlag == true)
				way += sBuf + "\n";
		}
		br.close();
	}

	private void setOutAndInEdges() {
		this.outEdges = new HashMap<Node, ArrayList<Edge>>();
		this.inEdges = new HashMap<Node, ArrayList<Edge>>();
		for (Edge edge : this.edgeSet) {
			Node nodeStart = edge.nodeStart;
			Node nodeEnd = edge.nodeEnd;
			if (this.outEdges.containsKey(nodeStart))
				this.outEdges.get(nodeStart).add(edge);
			else {
				ArrayList<Edge> edgeList = new ArrayList<Edge>();
				edgeList.add(edge);
				this.outEdges.put(nodeStart, edgeList);
			}
			if (this.inEdges.containsKey(nodeEnd))
				this.inEdges.get(nodeEnd).add(edge);
			else {
				ArrayList<Edge> edgeList = new ArrayList<Edge>();
				edgeList.add(edge);
				this.inEdges.put(nodeEnd, edgeList);
			}
		}
	}
}
