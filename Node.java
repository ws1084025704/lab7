package com.pair;

public class Node {
	public int weight;
	public Node next;
	public Graph_vertex link_vertex;

	public Node(Node next, Graph_vertex link_vertex) {
		weight = 1;
		this.next = next;
		this.link_vertex = link_vertex;
	}
}
