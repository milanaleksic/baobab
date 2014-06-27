package net.milanaleksic.baobab;

import net.milanaleksic.baobab.model.ModelBindingMetaData;
import org.eclipse.swt.widgets.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * User: Milan Aleksic
 * Date: 4/21/12
 * Time: 8:07 PM
 */
public class TransformationContext {

    private final ModelBindingMetaData modelBindingMetaData;

    private final Map<String, Object> mappedObjects;

    private final Composite root;

    public TransformationContext(Composite root, Map<String, Object> mappedObjects, ModelBindingMetaData modelBindingMetaData) {
        this.root = root;
        this.modelBindingMetaData = modelBindingMetaData;
        this.mappedObjects = Collections.unmodifiableMap(new HashMap<>(mappedObjects));
    }

    @SuppressWarnings("unchecked")
    public <T extends Composite> T getRoot() {
        return (T) root;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getMappedObject(String name) {
        return Optional.ofNullable((T) mappedObjects.get(name));
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
                ", root=" + root +
                '}';
    }

    public void showAndAwaitClosed() {
        if (!(root instanceof Shell))
            throw new TransformerException("Root must be of type Shell for you to execute this call");
        ((Shell) root).open();
        Display display = Display.getDefault();
        while (!root.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
    }
}
