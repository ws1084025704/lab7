package com.pair;

public class Graph_vertex {
	public String word;
	public Node links;
	public int children;

	public Graph_vertex(String word) {
		this.word = word;
		links = null;
		children = 0;
	}
}
