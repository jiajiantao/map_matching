package astar;

import map.Edge;
import map.Node;

public class NodeAttribute{
	public Node node;
	public Attribute attribute;
	
	public NodeAttribute(Node node, Node parent, Edge parentEdge, double g_value,
			double h_value, double f_value) {
		this.attribute = new Attribute(parent, parentEdge, g_value, h_value, f_value);
		this.node = node;
	}
	
	public NodeAttribute(Node node, Attribute attribute) {
		this.attribute = attribute;
		this.node = node;
	}

}
