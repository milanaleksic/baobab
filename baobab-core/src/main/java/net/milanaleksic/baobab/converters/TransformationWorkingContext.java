package net.milanaleksic.baobab.converters;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.milanaleksic.baobab.TransformationContext;
import net.milanaleksic.baobab.model.ModelBindingMetaData;
import net.milanaleksic.baobab.util.StringUtil;
import org.eclipse.swt.widgets.Composite;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * User: Milan Aleksic
 * Date: 6/25/12
 * Time: 1:31 PM
 * <p>
 * Adding is always delegated to hierarchy root context. Fetching all mapped objects
 * involves aggregating all tree elements up to current context (to cover independent
 * trees).
 * </p>
 */
public class TransformationWorkingContext {

    private final Map<String, Object> mappedObjects;

    private ModelBindingMetaData modelBindingMetaData;

    private boolean doNotCreateModalDialogs;

    private Object workItem;

    private final TransformationWorkingContext parentContext;
    private Object formObject;
    private String formLocation;

    public TransformationWorkingContext() {
        this(null);
    }

    public TransformationWorkingContext(@Nullable TransformationWorkingContext parentContext) {
        this.parentContext = parentContext;
        if (parentContext == null) {
            this.mappedObjects = Maps.newHashMap();
        } else {
            this.mappedObjects = ImmutableMap.of();
            this.formLocation = parentContext.formLocation;
        }
    }

    public void setDoNotCreateModalDialogs(boolean doNotCreateModalDialogs) {
        this.doNotCreateModalDialogs = doNotCreateModalDialogs;
    }

    public boolean isDoNotCreateModalDialogs() {
        return doNotCreateModalDialogs;
    }

    public Object getWorkItem() {
        return workItem;
    }

    public TransformationContext createTransformationContext() {
        Preconditions.checkArgument(workItem instanceof Composite, "You can't create TransformationContext if root of your hierarchy is a non-Composite class, in this case: " + workItem.getClass().getName());
        return new TransformationContext((Composite) workItem, getMutableRootMappedObjects(), modelBindingMetaData);
    }

    public ModelBindingMetaData getModelBindingMetaData() {
        return modelBindingMetaData;
    }

    public void setModelBindingMetaData(ModelBindingMetaData modelBindingMetaData) {
        this.modelBindingMetaData = modelBindingMetaData;
    }

    public Object getMappedObject(String key) {
        return getMutableRootMappedObjects().get(key);
    }

    public void mapObject(String key, Object object) {
        Preconditions.checkArgument(!StringUtil.isNullOrEmpty(key), "Object is not named");
        if (key.startsWith("_"))
            return;
        getMutableRootMappedObjects().put(key, object);
    }

    Map<String, Object> getMutableRootMappedObjects() {
        TransformationWorkingContext iterator = this;
        while (iterator != null && iterator.getParentContext() != null)
            iterator = iterator.getParentContext();
        return iterator == null ? null : iterator.mappedObjects;
    }

    public void setWorkItem(Object workItem) {
        this.workItem = workItem;
    }

    TransformationWorkingContext getParentContext() {
        return parentContext;
    }

    public Object getFormObject() {
        return formObject;
    }

    public void setFormObject(Object formObject) {
        this.formObject = formObject;
    }

    public void setFormLocation(String formLocation) {
        this.formLocation = formLocation;
    }

    public String getFormLocation() {
        return formLocation;
    }
}
