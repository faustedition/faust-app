package de.faustedition.model.transcription;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.faustedition.model.Portfolio;
import de.faustedition.model.Repository;
import de.faustedition.model.Transcription;
import de.faustedition.util.LoggingUtil;
import de.faustedition.util.XMLUtil;

public class TranscriptionStore implements InitializingBean {
	private String url;
	private String base = "/db";
	private String user;
	private String password;

	private XmlRpcClient rpcClient;

	@Required
	public void setUrl(String url) {
		this.url = url;
	}

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Collection<Repository> findRepositories() throws TranscriptionStoreException {
		String[] collectionNames = findCollections("/");
		List<Repository> repositories = new ArrayList<Repository>(collectionNames.length);
		for (String collection : collectionNames) {
			repositories.add(new Repository(collection));
		}
		return repositories;
	}

	public Collection<Portfolio> findPortfolios(Repository repository) throws TranscriptionStoreException {
		String[] portfolioNames = findCollections(repository.getPath());
		List<Portfolio> portfolios = new ArrayList<Portfolio>(portfolioNames.length);
		for (String portfolioName : portfolioNames) {
			portfolios.add(new Portfolio(repository, portfolioName));
		}
		return portfolios;
	}

	public Collection<Transcription> findTranscriptions(Portfolio portfolio) throws TranscriptionStoreException {
		String[] documents = findDocuments(portfolio.getPath());
		List<Transcription> transcriptions = new ArrayList<Transcription>(documents.length);
		for (String document : documents) {
			transcriptions.add(new Transcription(portfolio, document));
		}
		return transcriptions;
	}

	public Document retrieve(Transcription transcription) throws TranscriptionStoreException, SAXException, IOException {
		return XMLUtil.build(new ByteArrayInputStream(retrieve(transcription.getPath())));
	}

	@SuppressWarnings("unchecked")
	public String[] findDocuments(String path) throws TranscriptionStoreException {
		path = this.base + path;
		List<String> documents = new LinkedList<String>();
		try {
			Map<String, Object> collectionDesc = (Map<String, Object>) this.rpcClient.execute("getCollectionDesc", new Object[] { path });
			for (Object document : (Object[]) collectionDesc.get("documents")) {
				Map<String, Object> documentDesc = (Map<String, Object>) document;
				documents.add(documentDesc.get("name").toString());
			}
		} catch (XmlRpcException e) {
			throw new TranscriptionStoreException(String.format("RPC error finding documents in '%s'", path), e);
		}
		Collections.sort(documents);
		return documents.toArray(new String[documents.size()]);

	}

	@SuppressWarnings("unchecked")
	public String[] findCollections(String path) throws TranscriptionStoreException {
		path = this.base + path;
		List<String> collections = new LinkedList<String>();
		try {
			Map<String, Object> collectionDesc = (Map<String, Object>) this.rpcClient.execute("getCollectionDesc", new Object[] { path });
			for (Object collectionName : (Object[]) collectionDesc.get("collections")) {
				collections.add(collectionName.toString());
			}
		} catch (XmlRpcException e) {
			throw new TranscriptionStoreException(String.format("RPC error finding collections in '%s'", path), e);
		}
		Collections.sort(collections);
		return collections.toArray(new String[collections.size()]);
	}

	public byte[] retrieve(String path) throws TranscriptionStoreException {
		path = this.base + path;
		try {
			return (byte[]) rpcClient.execute("getDocument", new Object[] { path, Collections.EMPTY_MAP });
		} catch (XmlRpcException e) {
			throw new TranscriptionStoreException(String.format("RPC error retrieving document '%s'", path), e);
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		LoggingUtil.LOG.info(String.format("Transcription store: %s", url));
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setServerURL(new URL(url));
		if (StringUtils.isNotBlank(user)) {
			config.setBasicUserName(user);
			config.setBasicPassword(StringUtils.defaultString(password));
		}

		rpcClient = new XmlRpcClient();
		rpcClient.setConfig(config);
	}

}
