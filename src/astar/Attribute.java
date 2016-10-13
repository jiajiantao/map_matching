package astar;

import map.Node;
import map.Edge;

public class Attribute {
	public Node parent;
	public Edge parentEdge;
	public double g_value;
	public double h_value;
	public double f_value;

	public Attribute(Node parent, Edge parentEdge, double g_value, double h_value, double f_value){
		this.parent = parent;
		this.parentEdge = parentEdge;
		this.g_value = g_value;
		this.h_value = h_value;
		this.f_value = f_value;
	}
}
