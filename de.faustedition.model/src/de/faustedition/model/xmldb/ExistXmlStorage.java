package de.faustedition.model.xmldb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.xml.transform.TransformerException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import de.faustedition.util.ErrorUtil;
import de.faustedition.util.XMLUtil;

public class ExistXmlStorage implements InitializingBean {
	private String url;
	private HttpClient httpClient;

	@Required
	public void setUrl(String url) {
		this.url = url;
	}

	public void afterPropertiesSet() throws Exception {
		setUrl(url.endsWith("/") ? url : url + "/");

		MultiThreadedHttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
		httpConnectionManager.getParams().setDefaultMaxConnectionsPerHost(10);
		httpConnectionManager.getParams().setMaxTotalConnections(10);

		httpClient = new HttpClient(httpConnectionManager);
	}

	public Document get(String path, ExistQueryParameters parameters) throws ExistException, SAXException, IOException {
		GetMethod method = new GetMethod(constructURL(path));
		method.setQueryString(parameters.toNameValuePairs());
		return XMLUtil.build(new ByteArrayInputStream(retrieve(method)));
	}

	public void put(String path, final byte[] content, final String contentType) throws ExistException {
		PutMethod method = new PutMethod(constructURL(path));
		method.setRequestEntity(new RequestEntity() {

			public void writeRequest(OutputStream out) throws IOException {
				IOUtils.copy(new ByteArrayInputStream(content), out);
			}

			public boolean isRepeatable() {
				return false;
			}

			public String getContentType() {
				return contentType;
			}

			public long getContentLength() {
				return content.length;
			}
		});
		retrieve(method);
	}

	public void put(String path, final Document document) throws ExistException {
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			XMLUtil.serialize(document, byteArrayOutputStream);
			put(path, byteArrayOutputStream.toByteArray(), "application/xml");
		} catch (TransformerException e) {
			throw new ExistException("Unable to serialize XML document: " + e.getMessage());
		}
	}

	public void delete(String path) throws ExistException {
		retrieve(new DeleteMethod(constructURL(path)));
	}

	private byte[] retrieve(HttpMethod method) throws ExistException {
		InputStream responseBodyStream = null;
		try {
			int httpStatusCode = httpClient.executeMethod(method);
			byte[] responseBody = IOUtils.toByteArray(responseBodyStream = method.getResponseBodyAsStream());
			if (HttpStatus.SC_OK == httpStatusCode || HttpStatus.SC_CREATED == httpStatusCode) {
				return responseBody;
			} else {
				throw new ExistException(String.format("HTTP status: %d: %s", httpStatusCode, method.getStatusText()));
			}
		} catch (HttpException e) {
			throw createStorageError(e);
		} catch (IOException e) {
			throw createStorageError(e);
		} finally {
			IOUtils.closeQuietly(responseBodyStream);
			method.releaseConnection();
		}
	}

	private String constructURL(String collection) {
		try {
			collection = (collection == null ? "" : URLEncoder.encode(collection, "UTF-8"));
			return url + "/" + StringUtils.strip(collection, "/");
		} catch (UnsupportedEncodingException e) {
			throw ErrorUtil.fatal("Error encoding URL with UTF-8 charset", e);
		}
	}

	private ExistException createStorageError(Throwable cause) {
		return new ExistException(cause.getMessage());
	}

}
