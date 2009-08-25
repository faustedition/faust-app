package de.faustedition.model.store;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.jcr.Credentials;
import javax.jcr.NamespaceException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.JcrConstants;
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

import de.faustedition.util.ErrorUtil;
import de.faustedition.util.LoggingUtil;

@SuppressWarnings("deprecation")
public class ContentStore implements InitializingBean, DisposableBean {
	public static final String FAUST_NS_URI = "http://www.faustedition.net/ns";
	public static final String WORKSPACE = "store";
	private static final ClassPathResource STORE_CONFIG = new ClassPathResource("/jackrabbit-repository-config.xml");
	private static final ClassPathResource NODE_TYPE_DEFINITION_RESOURCE = new ClassPathResource("/jackrabbit-node-type-definitions.cnd");
	public static final Credentials ADMIN_CREDENTIALS = new SimpleCredentials("admin", "".toCharArray());

	private RepositoryImpl repository;
	private File dataDirectory;

	@Required
	public void setDataDirectory(File dataDirectory) {
		this.dataDirectory = dataDirectory;
	}

	public <T> T execute(ContentStoreCallback<T> callback) throws RepositoryException {
		Session session = repository.login(ADMIN_CREDENTIALS, WORKSPACE);
		try {
			return callback.inStore(session);
		} finally {
			session.logout();
		}
	}

	public boolean isEmpty() throws RepositoryException {
		return execute(new ContentStoreCallback<Boolean>() {

			@Override
			public Boolean inStore(Session session) throws RepositoryException {
				for (NodeIterator ni = session.getRootNode().getNodes(); ni.hasNext();) {
					if (!ni.nextNode().getName().equals(JcrConstants.JCR_SYSTEM)) {
						return false;
					}
				}

				return true;
			}
		});
	}

	public JackrabbitRepository getRepository() {
		return repository;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		File contentRepositoryBase = new File(dataDirectory, "content-repository");
		if (!contentRepositoryBase.isDirectory()) {
			Assert.isTrue(contentRepositoryBase.mkdirs(), "Cannot create content store directory");
		}

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
			public Object inStore(Session session) throws RepositoryException {
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

	public static String normalizeName(String name) {
		name = StringUtils.strip(name, "/").replaceAll(Pattern.quote("/"), "_");

		// umlauts
		name = name.replaceAll("\u00c4", "Ae").replaceAll("\u00e4", "ae");
		name = name.replaceAll("\u00d6", "Oe").replaceAll("\u00f6", "oe");
		name = name.replaceAll("\u00dc", "Ue").replaceAll("\u00fc", "ue");
		name = name.replaceAll("\u00df", "ss");

		// non-printable characters
		name = name.replaceAll("[^\\w\\.\\-]", "_");

		// condense underscores
		name = name.replaceAll("_+", "_");
		return name.trim();
	}

	public static String[] splitPath(String path) {
		return StringUtils.splitByWholeSeparator(normalizePath(path), "/");
	}

	public static String normalizePath(String path) {
		return StringUtils.trimToNull(StringUtils.strip(StringUtils.defaultString(path), "/").replaceAll("/+", "/"));
	}

	public static String getPath(ContentObject parent, String name) {
		return (parent == null ? "" : parent.getPath() + "/") + name;
	}

	public static boolean isValidName(String name) {
		return ContentStore.normalizeName(name).equals(name);
	}
}
