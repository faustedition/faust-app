package de.faustedition.collation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.goddag4j.Element;
import org.goddag4j.GoddagTreeNode;
import org.goddag4j.Text;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.resource.Finder;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.document.Document;
import de.faustedition.document.MaterialUnit;
import de.faustedition.document.MaterialUnitManager;
import de.faustedition.template.TemplateRepresentationFactory;

@Singleton
public class CollationFinder extends Finder {

	private final TemplateRepresentationFactory viewFactory;
	private final MaterialUnitManager materialUnitManager;
	private final GraphDatabaseService db;

	@Inject
	public CollationFinder(GraphDatabaseService db, TemplateRepresentationFactory viewFactory,
			MaterialUnitManager materialUnitManager) {
		this.db = db;
		this.viewFactory = viewFactory;
		this.materialUnitManager = materialUnitManager;
	}

	@Override
	public ServerResource find(Request request, Response response) {
		return new DemoResource();
	}

	public class DemoResource extends ServerResource {

		@Get("html")
		public Representation alignment() throws IOException {
			Transaction tx = db.beginTx();
			try {
				final Document document = materialUnitManager.find(new FaustURI(FaustAuthority.XML,
						"/document/faust/2.5/gsa_390883.xml"));

				final Element textRoot = document.getTranscript().getTrees().getRoot("f", "words");
				final Iterable<Token> textTokens = Iterables.transform(textRoot.getChildren(textRoot),
						new Function<GoddagTreeNode, Token>() {

							@Override
							public Token apply(GoddagTreeNode input) {
								return new GoddagToken(input, textRoot);
							}
						});

				final SortedSet<MaterialUnit> pages = document.getSortedContents();
				List<Iterable<Token>> pageTokens = new ArrayList<Iterable<Token>>(pages.size());
				for (MaterialUnit page : pages) {
					final Element documentRoot = page.getTranscript().getTrees().getRoot("f", "words");
					pageTokens.add(Iterables.transform(documentRoot.getChildren(documentRoot),
							new Function<GoddagTreeNode, Token>() {

								@Override
								public Token apply(GoddagTreeNode input) {
									return new GoddagToken(input, documentRoot);
								}
							}));
				}

				final List<Alignment> alignmentTable = new DiffCollator().align(textTokens,
						Iterables.concat(pageTokens));

				Map<String, Object> model = new HashMap<String, Object>();
				model.put("alignment",
						Lists.transform(alignmentTable, new Function<Alignment, AlignmentInformation>() {

							@Override
							public AlignmentInformation apply(Alignment input) {
								return new AlignmentInformation(input);
							}

						}));

				tx.success();
				return viewFactory.create("demo/collator", getClientInfo(), model);
			} finally {
				tx.finish();
			}
		}

	}

	public static class AlignmentInformation {
		private final String a;
		private final SortedSet<String> anodes;
		private final SortedSet<String> atags;
		private final String b;
		private final SortedSet<String> bnodes;
		private final SortedSet<String> btags;

		public AlignmentInformation(Alignment alignment) {
			final Token first = alignment.getFirst();
			a = (first == null ? null : first.text());
			anodes = (first == null ? new TreeSet<String>() : textNodes(first));
			atags = (first == null ? new TreeSet<String>() : tagging(first));

			final Token second = alignment.getSecond();
			b = second == null ? null : second.text();
			bnodes = (second == null ? new TreeSet<String>() : textNodes(second));
			btags = (second == null ? new TreeSet<String>() : tagging(second));
		}

		public String getA() {
			return a;
		}

		public SortedSet<String> getAnodes() {
			return anodes;
		}

		public SortedSet<String> getAtags() {
			return atags;
		}

		public String getB() {
			return b;
		}

		public SortedSet<String> getBnodes() {
			return bnodes;
		}

		public SortedSet<String> getBtags() {
			return btags;
		}

		private static SortedSet<String> textNodes(Token token) {
			if (!(token instanceof GoddagToken)) {
				return new TreeSet<String>();
			}
			final GoddagTreeNode node = ((GoddagToken) token).node;
			final Element root = ((GoddagToken) token).root;

			final SortedSet<String> textNodes = new TreeSet<String>();
			for (GoddagTreeNode child : node.getChildren(root)) {
				if (child instanceof Text) {
					textNodes.add("[#" + child.node.getId() + "]");
				}
			}
			return textNodes;
		}

		private static SortedSet<String> tagging(Token token) {
			if (!(token instanceof GoddagToken)) {
				return new TreeSet<String>();
			}

			final GoddagTreeNode node = ((GoddagToken) token).node;
			final Element root = ((GoddagToken) token).root;

			final SortedSet<String> tags = new TreeSet<String>();
			for (GoddagTreeNode child : node.getChildren(root)) {
				if (child instanceof Text) {
					for (Element textRoot : child.getRoots()) {
						for (GoddagTreeNode ancestor : child.getAncestors(textRoot)) {
							tags.add(((Element) ancestor).getQName());
						}
					}
				}
			}
			return tags;
		}
	}

}
