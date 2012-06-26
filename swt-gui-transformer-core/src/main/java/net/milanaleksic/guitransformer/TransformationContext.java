package net.milanaleksic.guitransformer;

import com.google.common.base.Optional;
import com.google.common.collect.*;
import org.eclipse.swt.widgets.Shell;

import java.util.Map;

/**
 * User: Milan Aleksic
 * Date: 4/21/12
 * Time: 8:07 PM
 */
public class TransformationContext {

    private final Map<String, Object> mappedObjects;

    private final Shell shell;

    public TransformationContext(Shell shell, Map<String, Object> mappedObjects) {
        this.shell = shell;
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

    public void dispose() {
    }

    public Map<String, Object> getMappedObjects() {
        return ImmutableMap.copyOf(mappedObjects);
    }
}
