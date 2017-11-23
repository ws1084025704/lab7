package com.pair;

import java.util.Stack;

public class Short_path {
	Short_path next;
	Stack<String> path;
	public Short_path(Stack<String> path, Short_path next) {
		this.next = next;
		this.path = path;
	}
	public Short_path clone() {
		return new Short_path(path, next);
	}
}
