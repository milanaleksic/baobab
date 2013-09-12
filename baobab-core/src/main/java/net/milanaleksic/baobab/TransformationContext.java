package net.milanaleksic.baobab;

import com.google.common.base.*;
import com.google.common.collect.ImmutableMap;
import net.milanaleksic.baobab.model.ModelBindingMetaData;
import org.eclipse.swt.widgets.*;

import java.util.Map;

public class TransformationContext {

    private final ModelBindingMetaData modelBindingMetaData;

    private final ImmutableMap<String, Object> mappedObjects;

    private final Object rootItem;

    public TransformationContext(Object rootItem, Map<String, Object> mappedObjects, ModelBindingMetaData modelBindingMetaData) {
        this.rootItem = rootItem;
        this.modelBindingMetaData = modelBindingMetaData;
        this.mappedObjects = ImmutableMap.copyOf(mappedObjects);
    }

    public Shell getShell() {
        checkRootItemIsShell();
        return (Shell) rootItem;
    }

    private void checkRootItemIsShell() {
        Preconditions.checkArgument(rootItem instanceof Shell, "TransformationContext does not contain Shell as hierarchy root");
    }

    public Object getRootItem() {
        return rootItem;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getMappedObject(String name) {
        Object object = mappedObjects.get(name);
        if (object == null)
            return Optional.absent();
        else
            return Optional.of((T) object);
    }

    public Map<String, Object> getMappedObjects() {
        return mappedObjects;
    }

    ModelBindingMetaData getModelBindingMetaData() {
        return modelBindingMetaData;
    }

    @Override
    public String toString() {
        return "TransformationContext{" +
                "modelBindingMetaData=" + modelBindingMetaData +
                ", mappedObjects=" + mappedObjects +
                ", rootItem=" + rootItem +
                "} " + super.toString();
    }

    public void showAndAwaitClosed() {
        final Shell shell = getShell();
        shell.open();
        Display display = Display.getDefault();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
    }
}
