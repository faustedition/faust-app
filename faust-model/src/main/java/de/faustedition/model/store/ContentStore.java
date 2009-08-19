package de.faustedition.model.store;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Credentials;
import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.api.JackrabbitNodeTypeManager;
import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.api.JackrabbitWorkspace;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.Assert;
import org.xml.sax.InputSource;

import de.faustedition.model.transcription.TranscriptionStore;
import de.faustedition.util.ErrorUtil;
import de.faustedition.util.LoggingUtil;

@SuppressWarnings("deprecation")
public class ContentStore implements InitializingBean, DisposableBean {
	public static final String FAUST_NS_URI = "http://www.faustedition.net/ns";
	public static final String WORKSPACE = "store";
	private static final ClassPathResource STORE_CONFIG = new ClassPathResource("/jackrabbit-repository-config.xml");
	private static final ClassPathResource NODE_TYPE_DEFINITION_RESOURCE = new ClassPathResource("/jackrabbit-node-type-definitions.cnd");
	public static final Credentials ADMIN_CREDENTIALS = new SimpleCredentials("admin", "".toCharArray());

	private Set<ContentObjectMapper<? extends ContentObject>> contentObjectMappers = new HashSet<ContentObjectMapper<? extends ContentObject>>();
	private Map<Class<? extends ContentObject>, ContentObjectMapper<? extends ContentObject>> contentObjectMapperRegistry = new HashMap<Class<? extends ContentObject>, ContentObjectMapper<? extends ContentObject>>();
	private RepositoryImpl repository;
	private String dataDirectory;

	@Required
	public void setDataDirectory(String dataDirectory) {
		this.dataDirectory = dataDirectory;
	}

	@Required
	public void setContentObjectMappers(Set<ContentObjectMapper<? extends ContentObject>> contentObjectMappers) {
		this.contentObjectMappers = contentObjectMappers;
		for (ContentObjectMapper<? extends ContentObject> mapper : contentObjectMappers) {
			contentObjectMapperRegistry.put(mapper.getMappedType(), mapper);
		}
	}

	public TranscriptionStore findTranscriptionStore() throws RepositoryException {
		List<TranscriptionStore> stores = list(null, TranscriptionStore.class);
		return (stores.isEmpty() ? null : stores.get(0));
	}

	public <T extends ContentObject> List<T> list(final ContentObject parent, final Class<T> classFilter) throws RepositoryException {
		return execute(new ContentStoreCallback<List<T>>() {

			@SuppressWarnings("unchecked")
			@Override
			public List<T> doInSession(Session session) throws RepositoryException {
				Node parentNode = session.getRootNode();
				if (parent != null) {
					parentNode = parentNode.getNode(parent.getPath());
				}

				List<T> list = new LinkedList<T>();
				for (NodeIterator ni = parentNode.getNodes(); ni.hasNext();) {
					ContentObject contentObject = build(session, ni.nextNode().getPath());
					if ((contentObject != null) && (classFilter.isAssignableFrom(contentObject.getClass()))) {
						list.add((T) contentObject);
					}
				}
				Collections.sort(list);
				return list;
			}
		});
	}

	public List<ContentObject> traverse(final String path) throws RepositoryException {
		return execute(new ContentStoreCallback<List<ContentObject>>() {

			@Override
			public List<ContentObject> doInSession(Session session) throws RepositoryException {
				String[] pathComponents = ContentStoreUtil.splitPath(path);

				Node node = session.getRootNode();
				List<ContentObject> list = new LinkedList<ContentObject>();
				for (int i = 0; i < pathComponents.length; i++) {
					node = node.getNode(pathComponents[i]);
					list.add(build(session, node.getPath()));
				}
				return list;
			}
		});
	}

	public ContentObject get(final String path) throws RepositoryException {
		return execute(new ContentStoreCallback<ContentObject>() {

			@Override
			public ContentObject doInSession(Session session) throws RepositoryException {
				return build(session, path);
			}
		});
	}

	@SuppressWarnings("unchecked")
	public <T extends ContentObject> T save(final T contentObject) throws RepositoryException {
		final ContentObjectMapper<T> contentObjectMapper = (ContentObjectMapper<T>) contentObjectMapperRegistry.get(contentObject.getClass());
		if (contentObjectMapper == null) {
			throw new IllegalArgumentException(contentObject.toString());
		}
		return execute(new ContentStoreCallback<T>() {

			@Override
			public T doInSession(Session session) throws RepositoryException {
				Node rootNode = session.getRootNode();
				String nodePath = contentObject.getPath();
				Node node = rootNode.hasNode(nodePath) ? rootNode.getNode(nodePath) : rootNode.addNode(nodePath, contentObjectMapper.getNodeType());
				contentObjectMapper.save(contentObject, node);
				session.save();
				return contentObject;
			}
		});
	}

	public <T extends ContentObject> void delete(final T contentObject) throws RepositoryException {
		execute(new ContentStoreCallback<T>() {

			@Override
			public T doInSession(Session session) throws RepositoryException {
				contentObject.getNode(session).remove();
				session.save();
				return null;
			}
		});
	}
	
	protected ContentObject build(Session session, String path) throws RepositoryException {
		Node node = session.getRootNode().getNode(ContentStoreUtil.normalizePath(path));
		for (ContentObjectMapper<? extends ContentObject> mapper : contentObjectMappers) {
			if (mapper.mapsObjectFor(node)) {
				return mapper.map(node);
			}
		}
		return null;
	}

	public <T> T execute(ContentStoreCallback<T> callback) throws RepositoryException {
		Session session = repository.login(ADMIN_CREDENTIALS, WORKSPACE);
		try {
			return callback.doInSession(session);
		} finally {
			session.logout();
		}
	}

	public boolean isEmpty() throws RepositoryException {
		return list(null, ContentObject.class).isEmpty();
	}

	public JackrabbitRepository getRepository() {
		return repository;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		File contentRepositoryBase = new File(dataDirectory, "content-repository");
		if (!contentRepositoryBase.exists()) {
			Assert.isTrue(contentRepositoryBase.mkdirs(), "Cannot create content store directory");
		}
		Assert.isTrue(contentRepositoryBase.isDirectory() && contentRepositoryBase.canWrite(), String.format("Cannot access content store directory '%s'", contentRepositoryBase
				.getAbsolutePath()));

		RepositoryConfig repositoryConfig = RepositoryConfig.create(new InputSource(STORE_CONFIG.getInputStream()), contentRepositoryBase.getAbsolutePath());
		repository = RepositoryImpl.create(repositoryConfig);

		Session session = repository.login(ADMIN_CREDENTIALS);
		try {
			boolean workspaceExists = false;
			for (String workspaceName : session.getWorkspace().getAccessibleWorkspaceNames()) {
				if (WORKSPACE.equals(workspaceName)) {
					workspaceExists = true;
				}
			}

			if (!workspaceExists) {
				LoggingUtil.LOG.info("Creating workspace '" + WORKSPACE + "'");
				((JackrabbitWorkspace) session.getWorkspace()).createWorkspace(WORKSPACE);
			}
		} finally {
			session.logout();
		}

		execute(new ContentStoreCallback<Object>() {

			@Override
			public Object doInSession(Session session) throws RepositoryException {
				NamespaceRegistry namespaceRegistry = session.getWorkspace().getNamespaceRegistry();
				try {
					Assert.isTrue("faust".equals(namespaceRegistry.getPrefix(FAUST_NS_URI)));
				} catch (NamespaceException e) {
					LoggingUtil.LOG.info("Registering namespace '" + FAUST_NS_URI + "' in content repository");
					namespaceRegistry.registerNamespace("faust", FAUST_NS_URI);
				}

				JackrabbitNodeTypeManager nodeTypeManager = (JackrabbitNodeTypeManager) session.getWorkspace().getNodeTypeManager();
				if (!nodeTypeManager.hasNodeType("faust:metadata")) {
					try {
						nodeTypeManager.registerNodeTypes(NODE_TYPE_DEFINITION_RESOURCE.getInputStream(), JackrabbitNodeTypeManager.TEXT_X_JCR_CND);
					} catch (IOException e) {
						throw ErrorUtil.fatal("Error registering node types in content-repository", e);
					}
				}

				return null;
			}

		});
	}

	@Override
	public void destroy() throws Exception {
		repository.shutdown();
	}
}
