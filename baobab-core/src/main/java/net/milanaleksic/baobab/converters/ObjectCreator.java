package net.milanaleksic.baobab.converters;

import com.google.common.base.Preconditions;
import net.milanaleksic.baobab.TransformerException;
import net.milanaleksic.baobab.providers.*;
import org.codehaus.jackson.JsonNode;
import org.eclipse.swt.SWT;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * User: Milan Aleksic (milanaleksic@gmail.com)
 * Date: 27/08/2013
 */
public abstract class ObjectCreator {

    public static final int DEFAULT_STYLE_SHELL = SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL;
    public static final int DEFAULT_STYLE_REST = SWT.NONE;

    @Inject
    protected NodeProcessor nodeProcessor;

    @Inject
    private ShortcutsProvider shortcutsProvider;

    @Inject
    protected ConverterProvider converterProvider;

    protected Class<?> deduceClassFromNode(JsonNode valueNode) {
        Preconditions.checkArgument(valueNode.has(ObjectConverter.KEY_SPECIAL_TYPE), "Item definition does not define type");
        String classIdentifier = valueNode.get(ObjectConverter.KEY_SPECIAL_TYPE).asText();
        return deduceClassFromNode(classIdentifier);
    }

    protected Class<?> deduceClassFromNode(String classIdentifier) {
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

    protected int fixStyleIfNoModalDialogs(TransformationWorkingContext context, int style) {
        if (context.isDoNotCreateModalDialogs()) {
            style = style & (~SWT.APPLICATION_MODAL);
            style = style & (~SWT.SYSTEM_MODAL);
            style = style & (~SWT.PRIMARY_MODAL);
        }
        return style;
    }

    public TransformationWorkingContext create(TransformationWorkingContext context, String key, JsonNode value) {
        if (isWidgetUsingBuilder(key, value))
            return createWidgetUsingBuilder(context, key, value);
        else
            return createWidgetUsingClassInstantiation(context, key, value);
    }

    public abstract boolean isEligibleForItem(String key, JsonNode value);

    public abstract boolean isWidgetUsingBuilder(String key, JsonNode value);

    public abstract TransformationWorkingContext createWidgetUsingBuilder(TransformationWorkingContext context, @Nullable String key, JsonNode value);

    public abstract TransformationWorkingContext createWidgetUsingClassInstantiation(TransformationWorkingContext context, @Nullable String key, JsonNode objectDefinition);

}
