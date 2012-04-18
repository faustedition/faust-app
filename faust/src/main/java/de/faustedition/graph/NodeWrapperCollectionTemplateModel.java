package de.faustedition.graph;

import freemarker.ext.beans.BeanModel;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.SimpleCollection;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelIterator;

public class NodeWrapperCollectionTemplateModel implements TemplateCollectionModel, TemplateHashModel {

	private final NodeWrapperCollection<?> collection;
	private TemplateCollectionModel collectionModel;
	private BeanModel beanModel;

	public NodeWrapperCollectionTemplateModel(NodeWrapperCollection<?> collection) {
		this.collection = collection;
	}

	public NodeWrapperCollection<?> getCollection() {
		return collection;
	}

	@Override
	public TemplateModel get(String key) throws TemplateModelException {
		return beanModel().get(key);
	}

	@Override
	public boolean isEmpty() throws TemplateModelException {
		return beanModel().isEmpty();
	}

	@Override
	public TemplateModelIterator iterator() throws TemplateModelException {
		return collectionModel().iterator();
	}

	protected TemplateCollectionModel collectionModel() {
		if (collectionModel == null) {
			collectionModel = new SimpleCollection(collection);
		}
		return collectionModel;
	}

	protected BeanModel beanModel() throws TemplateModelException {
		if (beanModel == null) {
			beanModel = new BeanModel(collection, BeansWrapper.getDefaultInstance());
		}
		return beanModel;
	}
}
