package net.milanaleksic.guitransformer.converters;

import com.google.common.base.Preconditions;
import net.milanaleksic.guitransformer.TransformerException;
import net.milanaleksic.guitransformer.providers.*;
import org.codehaus.jackson.JsonNode;
import org.eclipse.swt.SWT;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
* User: Milan Aleksic
* Date: 2/5/13
* Time: 2:20 PM
*/
abstract class ObjectCreator {

    public static final int DEFAULT_STYLE_SHELL = SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL;
    public static final int DEFAULT_STYLE_REST = SWT.NONE;

    @Inject
    protected ObjectConverter objectConverter;

    @Inject
    private ShortcutsProvider shortcutsProvider;

    @Inject
    protected ConverterProvider converterProvider;

    protected abstract boolean isWidgetUsingBuilder(String key, JsonNode value);

    protected abstract TransformationWorkingContext createWidgetUsingBuilder(TransformationWorkingContext context, @Nullable String key, JsonNode value);

    protected abstract TransformationWorkingContext createWidgetUsingClassInstantiation(TransformationWorkingContext context, @Nullable String key, JsonNode objectDefinition)
            throws IllegalAccessException, InstantiationException, InvocationTargetException;

    public TransformationWorkingContext create(TransformationWorkingContext context, @Nullable String key, JsonNode value) {
        try {
            TransformationWorkingContext ofTheJedi = isWidgetUsingBuilder(key, value)
                    ? createWidgetUsingBuilder(context, key, value)
                    : createWidgetUsingClassInstantiation(context, key, value);
            transformNodeToProperties(ofTheJedi, value);
            return ofTheJedi;
        } catch (TransformerException e) {
            throw e;
        } catch (Exception e) {
            throw new TransformerException("Widget creation failed", e);
        }
    }

    private void transformNodeToProperties(TransformationWorkingContext context, JsonNode jsonNode) {
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.getFields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            if (field.getKey().equals(ObjectConverter.KEY_SPECIAL_CHILDREN))
                objectConverter.transformChildren(context, field.getValue());
            else
                objectConverter.transformSingleJsonNode(context, field);
        }
    }

    int fixStyleIfNoModalDialogs(TransformationWorkingContext context, int style) {
        if (context.isDoNotCreateModalDialogs()) {
            style = style & (~SWT.APPLICATION_MODAL);
            style = style & (~SWT.SYSTEM_MODAL);
            style = style & (~SWT.PRIMARY_MODAL);
        }
        return style;
    }

    Class<?> deduceClassFromNode(JsonNode valueNode) {
        Preconditions.checkArgument(valueNode.has(ObjectConverter.KEY_SPECIAL_TYPE), "Item definition does not define type");
        String classIdentifier = valueNode.get(ObjectConverter.KEY_SPECIAL_TYPE).asText();
        return deduceClassFromNode(classIdentifier);
    }

    Class<?> deduceClassFromNode(String classIdentifier) {
        Class<?> aClass = shortcutsProvider.provideClassForShortcut(classIdentifier);
        if (aClass != null)
            return aClass;
        else
            try {
                return Class.forName(classIdentifier);
            } catch (ClassNotFoundException e) {
                throw new TransformerException("Class was not found: " + classIdentifier, e);
            }
    }
}
