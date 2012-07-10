package de.faustedition.graph;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import org.neo4j.graphdb.Node;

import javax.annotation.Nullable;

public abstract class NodeWrapper {

	public final Node node;

	protected NodeWrapper(Node node) {
		this.node = node;
	}

	protected static <T extends NodeWrapper> T newInstance(Class<T> type, Node node) {
		try {
			return type.getConstructor(Node.class).newInstance(node);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected static <T extends NodeWrapper> Function<Node, T> newWrapperFunction(final Class<T> type) {
		return new Function<Node, T>() {
			@Override
			public T apply(@Nullable Node input) {
				return newInstance(type, input);
			}
		};
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof NodeWrapper) {
			return node.equals(((NodeWrapper) obj).node);
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return node.hashCode();
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).addValue(node).toString();
	}
}
