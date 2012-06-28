package net.milanaleksic.guitransformer;

import com.google.common.base.*;
import com.google.common.collect.*;
import org.eclipse.swt.widgets.Shell;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * User: Milan Aleksic
 * Date: 6/25/12
 * Time: 1:31 PM
 *
 * Adding is always delegated to hierarchy root context. Fetching all mapped objects
 * involves aggregating all tree elements up to current context (to cover independent
 * trees).
 */
class TransformationWorkingContext {

    private final Map<String, Object> mappedObjects;

    private boolean doNotCreateModalDialogs;

    private Object workItem;

    private final TransformationWorkingContext parentContext;

    public TransformationWorkingContext() {
        this(null);
    }

    public TransformationWorkingContext(@Nullable TransformationWorkingContext context) {
        parentContext = context;
        mappedObjects = context == null ? Maps.<String, Object>newHashMap() : ImmutableMap.<String, Object>of();
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
        Preconditions.checkArgument(workItem instanceof Shell, "You can't create TransformationContext for a non-Shell hierarchy root, class="+workItem.getClass().getName());
        return new TransformationContext((Shell) workItem, getMappedObjects());
    }

    public Object getMappedObject(String key) {
        return getRootMappedObjects().get(key);
    }

    public void mapAll(Map<String, Object> mappedObjects) {
        getRootMappedObjects().putAll(mappedObjects);
    }

    public void mapObject(String key, Object object) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(key));
        if (key.startsWith("_"))
            return;
        getRootMappedObjects().put(key, object);
    }

    private Map<String, Object> getRootMappedObjects() {
        TransformationWorkingContext iterator = this;
        while (iterator.getParentContext() != null)
            iterator = iterator.getParentContext();
        return iterator.mappedObjects;
    }

    public Map<String, Object> getMappedObjects() {
        final Map<String, Object> temp = Maps.newHashMap();
        temp.putAll(mappedObjects);
        TransformationWorkingContext iterator = getParentContext();
        while (iterator != null) {
            temp.putAll(iterator.getMappedObjects());
            iterator = iterator.getParentContext();
        }
        return ImmutableMap.copyOf(temp);
    }

    public void setWorkItem(Object workItem) {
        this.workItem = workItem;
    }

    TransformationWorkingContext getParentContext() {
        return parentContext;
    }
}
