package de.faustedition.text;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.codehaus.jackson.JsonGenerator;
import org.goddag4j.Element;
import org.goddag4j.GoddagNode.NodeType;
import org.goddag4j.GoddagTreeNode;
import org.goddag4j.io.GoddagJSONWriter.GoddagJSONEnhancer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Objects;

import de.faustedition.document.Document;
import de.faustedition.genesis.GeneticRelationManager;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TextGeneticJSONEnhancer implements GoddagJSONEnhancer {

	@Autowired
	private GeneticRelationManager geneticRelationManager;

	private final Map<Document, SortedSet<LineInterval>> geneticRelations = new HashMap<Document, SortedSet<LineInterval>>();

	private int intervalStart = Integer.MAX_VALUE;
	private int intervalEnd = 0;

	@Override
	public void enhance(GoddagTreeNode node, NodeType nt, JsonGenerator out) throws IOException {
		if (nt != NodeType.ELEMENT) {
			return;
		}

		Element element = (Element) node;
		if (!"tei:l".equals(element.getQName())) {
			return;
		}

		final String lnStr = element.getAttributeValue("tei", "n");
		if (lnStr == null) {
			return;
		}

		final int ln = Integer.parseInt(lnStr);
		intervalStart = Math.min(intervalStart, ln);
		intervalEnd = Math.max(intervalEnd, ln);

		final Set<Document> relatedDocuments = geneticRelationManager.findRelatedDocuments(element);
		for (Document d : relatedDocuments) {
			SortedSet<LineInterval> intervals = geneticRelations.get(d);
			if (intervals == null) {
				geneticRelations.put(d, intervals = new TreeSet<LineInterval>());
			}

			boolean found = false;
			for (Iterator<LineInterval> liIt = intervals.iterator(); liIt.hasNext();) {
				LineInterval li = liIt.next();
				if (li.covers(ln)) {
					found = true;
					break;
				} else if (li.adjacentTo(ln)) {
					liIt.remove();
					intervals.add(li.extendBy(ln));
					found = true;
					break;
				}
			}
			if (!found) {
				intervals.add(new LineInterval(ln));
			}
		}
		out.writeStartObject();
		out.writeNumberField("geneticRelations", relatedDocuments.size());
		out.writeEndObject();
	}

	@Override
	public void enhance(JsonGenerator out) throws IOException {
		out.writeObjectFieldStart("geneticRelations");

		out.writeArrayFieldStart("interval");
		out.writeNumber(intervalStart);
		out.writeNumber(intervalEnd);
		out.writeEndArray();

		out.writeArrayFieldStart("related");
		for (Map.Entry<Document, SortedSet<LineInterval>> related : geneticRelations.entrySet()) {
			final Document document = related.getKey();
			String sigil = document.getMetadataValue("wa-id");
			sigil = (sigil == null ? document.getMetadataValue("callnumber") : sigil);

			out.writeStartObject();
			out.writeStringField("document", document.getSource().toString());
			out.writeStringField("sigil", sigil);

			out.writeArrayFieldStart("intervals");
			final SortedSet<LineInterval> intervals = related.getValue();
			final SortedSet<LineInterval> condensed = new TreeSet<LineInterval>();
			LineInterval last = null;
			for (LineInterval li : intervals) {
				if (last == null) {
					condensed.add(last = li);
					continue;
				}
				if (li.follows(last)) {
					last.merge(li);
				} else {
					condensed.add(last);
					condensed.add(last = li);
				}
			}
			for (LineInterval li : condensed) {
				out.writeStartArray();
				out.writeNumber(li.start);
				out.writeNumber(li.end);
				out.writeEndArray();
			}
			out.writeEndArray();
			out.writeEndObject();
		}
		out.writeEndArray();
		out.writeEndObject();
	}

	private static class LineInterval implements Comparable<LineInterval> {
		private int start;
		private int end;

		private LineInterval(int start, int end) {
			this.start = start;
			this.end = end;
		}

		public void merge(LineInterval next) {
			this.end = next.end;
		}

		public boolean follows(LineInterval prev) {
			return prev.end == (start - 1);
		}

		private LineInterval(int start) {
			this(start, start);
		}

		public LineInterval extendBy(int ln) {
			return ln < start ? new LineInterval(ln, end) : new LineInterval(start, ln);
		}

		public boolean adjacentTo(int ln) {
			return (ln == start - 1) || (ln == end + 1);
		}

		public boolean covers(int ln) {
			return (start <= ln && end >= ln);
		}

		@Override
		public int compareTo(LineInterval o) {
			return (start == o.start ? end - o.end : start - o.start);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj != null && obj instanceof LineInterval) {
				LineInterval o = (LineInterval) obj;
				return start == o.start && end == o.end;
			}
			return super.equals(obj);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(start, end);
		}
	}
}
