package de.faustedition.template;

import de.faustedition.graph.NodeWrapperCollection;
import de.faustedition.graph.NodeWrapperCollectionTemplateModel;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class TemplateObjectWrapper extends DefaultObjectWrapper {

    @Override
    public TemplateModel wrap(Object obj) throws TemplateModelException {
        if (obj instanceof NodeWrapperCollection) {
            return new NodeWrapperCollectionTemplateModel((NodeWrapperCollection<?>) obj);
        }
        return super.wrap(obj);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object unwrap(TemplateModel model, Class hint) throws TemplateModelException {
        if (model instanceof NodeWrapperCollectionTemplateModel) {
            return ((NodeWrapperCollectionTemplateModel) model).getCollection();
        }
        return super.unwrap(model, hint);
    }
}
