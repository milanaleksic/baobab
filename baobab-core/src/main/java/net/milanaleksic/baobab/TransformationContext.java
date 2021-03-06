package net.milanaleksic.baobab;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import net.milanaleksic.baobab.model.ModelBindingMetaData;
import org.eclipse.swt.widgets.*;

import java.util.Map;

/**
 * User: Milan Aleksic
 * Date: 4/21/12
 * Time: 8:07 PM
 */
public class TransformationContext {

    private final ModelBindingMetaData modelBindingMetaData;

    private final ImmutableMap<String, Object> mappedObjects;

    private final Shell shell;

    public TransformationContext(Shell shell, Map<String, Object> mappedObjects, ModelBindingMetaData modelBindingMetaData) {
        this.shell = shell;
        this.modelBindingMetaData = modelBindingMetaData;
        this.mappedObjects = ImmutableMap.copyOf(mappedObjects);
    }

    public Shell getShell() {
        return shell;
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
                ", shell=" + shell +
                '}';
    }

    public void showAndAwaitClosed() {
        shell.open();
        Display display = Display.getDefault();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
    }
}
