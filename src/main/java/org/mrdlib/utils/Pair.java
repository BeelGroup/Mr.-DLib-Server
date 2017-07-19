package org.mrdlib.utils;

public class Pair<L, R> {

	private final L left;
	private final R right;

	public Pair(L left, R right) {
		this.left = left;
		this.right = right;
	}

	public L getLeft() {
		return left;
	}

	public R getRight() {
		return right;
	}

	public L getKey() {
		return left;
	}

	public R getValue() {
		return right;
	}

	@Override
	public int hashCode() {
		return left.hashCode() ^ right.hashCode();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object o) {
		Pair<L,R> pairo = null;
		if (!(o instanceof Pair<?,?>))
			return false;
		else {
			pairo = (Pair<L,R>) o;
			return this.left.equals(pairo.getLeft()) && this.right.equals(pairo.getRight());
		}
	}

}