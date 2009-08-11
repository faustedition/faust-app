package de.faustedition.model.transcription;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.JcrConstants;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.model.Portfolio;
import de.faustedition.model.Repository;
import de.faustedition.model.Transcription;
import de.faustedition.model.store.ContentContainer;
import de.faustedition.model.store.ContentStore;
import de.faustedition.model.store.ContentStoreCallback;
import de.faustedition.model.store.ObjectNotFoundException;
import de.faustedition.util.ErrorUtil;

public class TranscriptionStore {
	private static final String TRANSCRIPTION_SUFFIX = ".xml";
	private static final String TRANSCRIPTIONS_NODE_NAME = "transcriptions";

	@Autowired
	private ContentStore contentStore;

	public Repository[] findRepositories() throws RepositoryException {
		return contentStore.execute(new ContentStoreCallback<Repository[]>() {

			@Override
			public Repository[] doInSession(Session session) throws RepositoryException {
				String[] repositoryNames = findChildNodeNames(getTranscriptionsNode(session));
				Repository[] repositories = new Repository[repositoryNames.length];
				for (int i = 0; i < repositoryNames.length; i++) {
					repositories[i] = new Repository(repositoryNames[i]);
				}
				return repositories;
			}
		});
	}

	public Repository findRepository(final String name) throws RepositoryException {
		return contentStore.execute(new ContentStoreCallback<Repository>() {
			@Override
			public Repository doInSession(Session session) throws RepositoryException {
				return (hasChild(getTranscriptionsNode(session), name) ? new Repository(name) : null);
			}
		});
	}

	public Portfolio[] findPortfolios(final Repository repository) throws RepositoryException {
		return contentStore.execute(new ContentStoreCallback<Portfolio[]>() {

			@Override
			public Portfolio[] doInSession(Session session) throws RepositoryException {
				String[] portfolioNames = findChildNodeNames(getTranscriptionsNode(session).getNode(repository.getPath()));
				Portfolio[] portfolios = new Portfolio[portfolioNames.length];
				for (int i = 0; i < portfolioNames.length; i++) {
					portfolios[i] = new Portfolio(repository, portfolioNames[i]);
				}
				return portfolios;
			}
		});
	}

	public Portfolio findPortfolio(final Repository repository, final String name) throws RepositoryException {
		return contentStore.execute(new ContentStoreCallback<Portfolio>() {
			@Override
			public Portfolio doInSession(Session session) throws RepositoryException {
				return (hasChild(getTranscriptionsNode(session).getNode(repository.getPath()), name) ? new Portfolio(repository, name) : null);
			}
		});
	}

	public Transcription[] findTranscriptions(final Portfolio portfolio) throws RepositoryException {
		return contentStore.execute(new ContentStoreCallback<Transcription[]>() {

			@Override
			public Transcription[] doInSession(Session session) throws RepositoryException {
				String[] transcriptionNames = findChildNodeNames(getTranscriptionsNode(session).getNode(portfolio.getPath()));
				List<Transcription> transcriptions = new ArrayList<Transcription>(transcriptionNames.length);
				for (int i = 0; i < transcriptionNames.length; i++) {
					String name = transcriptionNames[i];
					if (name.endsWith(TRANSCRIPTION_SUFFIX)) {
						transcriptions.add(new Transcription(portfolio, StringUtils.removeEnd(name, TRANSCRIPTION_SUFFIX)));
					}
				}
				return transcriptions.toArray(new Transcription[transcriptions.size()]);
			}
		});
	}

	public Transcription findTranscription(final Portfolio portfolio, final String name) throws RepositoryException {
		return contentStore.execute(new ContentStoreCallback<Transcription>() {
			@Override
			public Transcription doInSession(Session session) throws RepositoryException {
				return (hasChild(getTranscriptionsNode(session).getNode(portfolio.getPath()), name + TRANSCRIPTION_SUFFIX) ? new Transcription(portfolio, name) : null);
			}
		});
	}

	public List<ContentContainer> traverse(String path) throws RepositoryException, ObjectNotFoundException {
		String[] pathComponents = StringUtils.splitByWholeSeparator(StringUtils.strip(path, "/"), "/");
		List<ContentContainer> traversalList = new ArrayList<ContentContainer>(pathComponents.length);

		if (pathComponents.length < 1) {
			return traversalList;
		}

		Repository repository = findRepository(pathComponents[0]);
		if (repository == null) {
			throw new ObjectNotFoundException(path);
		}

		traversalList.add(repository);
		if (pathComponents.length < 2) {
			return traversalList;
		}

		Portfolio portfolio = findPortfolio(repository, pathComponents[1]);
		if (portfolio == null) {
			throw new ObjectNotFoundException(path);
		}

		traversalList.add(portfolio);
		if (pathComponents.length < 3) {
			return traversalList;
		}

		Transcription transcription = findTranscription(portfolio, pathComponents[2]);
		if (transcription == null) {
			throw new ObjectNotFoundException(path);
		}

		traversalList.add(transcription);
		return traversalList;
	}

	public byte[] retrieve(final Transcription transcription) throws RepositoryException {
		return contentStore.execute(new ContentStoreCallback<byte[]>() {

			@Override
			public byte[] doInSession(Session session) throws RepositoryException {
				Node transcriptionNode = getTranscriptionsNode(session).getNode(transcription.getPath() + TRANSCRIPTION_SUFFIX);
				Value transcriptionData = transcriptionNode.getNode(JcrConstants.JCR_CONTENT).getProperty(JcrConstants.JCR_DATA).getValue();
				InputStream dataStream = null;
				try {
					return IOUtils.toByteArray(dataStream = transcriptionData.getStream());
				} catch (IOException e) {
					throw ErrorUtil.fatal("Error while reading transcription data", e);
				} finally {
					IOUtils.closeQuietly(dataStream);
				}
			}
		});

	}

	protected Node getTranscriptionsNode(Session session) throws RepositoryException {
		return session.getRootNode().getNode(TRANSCRIPTIONS_NODE_NAME);
	}

	protected boolean hasChild(Node node, String childName) throws RepositoryException {
		try {
			node.getNode(childName);
			return true;
		} catch (PathNotFoundException e) {
			return false;
		}
	}

	protected String[] findChildNodeNames(Node node) throws RepositoryException {
		NodeIterator childNodes = node.getNodes();
		List<String> childNodeNames = new ArrayList<String>((int) childNodes.getSize());
		while (childNodes.hasNext()) {
			childNodeNames.add(childNodes.nextNode().getName());
		}
		Collections.sort(childNodeNames);
		return childNodeNames.toArray(new String[childNodeNames.size()]);
	}
}
