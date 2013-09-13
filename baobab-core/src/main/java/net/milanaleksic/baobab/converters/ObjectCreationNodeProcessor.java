package net.milanaleksic.baobab.converters;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.milanaleksic.baobab.TransformerException;
import net.milanaleksic.baobab.builders.Builder;
import net.milanaleksic.baobab.builders.BuilderContext;
import net.milanaleksic.baobab.providers.BuilderProvider;
import net.milanaleksic.baobab.providers.ConverterProvider;
import net.milanaleksic.baobab.util.WidgetCreator;
import org.codehaus.jackson.JsonNode;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static net.milanaleksic.baobab.util.ObjectUtil.*;

public class ObjectCreationNodeProcessor implements NodeProcessor {

    @Inject
    private ConverterProvider converterProvider;

    @Inject
    private BuilderProvider builderProvider;

    @Override
    public BuilderContext<?> visitBuilderNotationItem(TransformationWorkingContext context, String builderName, String parameters) {
        final List<String> params = Lists.newArrayList(Splitter.on(",").trimResults().split(parameters));
        final Builder<?> builder = builderProvider.provideBuilderForName(builderName);
        if (builder == null)
            throw new TransformerException("Builder is not registered: " + builderName);
        return builder.create(context.getWorkItem(), params);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visitSingleField(TransformationWorkingContext context, String propertyName, JsonNode propertyNode) {
        try {
            Optional<Method> method = getSetterByName(context.getWorkItem(), getSetterForField(propertyName));
            if (method.isPresent()) {
                Class<?> argType = method.get().getParameterTypes()[0];
                Converter converter = converterProvider.provideConverterForClass(argType);
                Object value = converter.getValueFromJson(context.getWorkItem(), propertyNode, context.getMutableRootMappedObjects());
                method.get().invoke(context.getWorkItem(), value);
            } else {
                Optional<Field> fieldByName = getFieldByName(context.getWorkItem(), propertyName);
                if (fieldByName.isPresent()) {
                    Class<?> argType = fieldByName.get().getType();
                    Converter converter = converterProvider.provideConverterForClass(argType);
                    Object value = converter.getValueFromJson(context.getWorkItem(), propertyNode, context.getMutableRootMappedObjects());
                    fieldByName.get().set(context.getWorkItem(), value);
                } else
                    throw new TransformerException("No setter nor field " + propertyName + " could be found in class " + context.getWorkItem().getClass().getName() + "; context: " + propertyNode);
            }
        } catch (TransformerException e) {
            throw e;
        } catch (Throwable t) {
            throw new TransformerException("Transformation was not successful", t);
        }
    }

    @Override
    public <T> T visitObjectItem(Class<T> widgetClass, Object parent, int style) {
        try {
            return WidgetCreator.get(widgetClass).newInstance(parent, style);
        } catch (Exception e) {
            throw new TransformerException("Unexpected exception encountered while processing widget creation, widgetClass=" + widgetClass.getName() + ", parent=" + parent + ", style=" + style, e);
        } catch (VerifyError error) {
            throw new TransformerException("Code generation verify error encountered while processing widget creation, widgetClass=" + widgetClass.getName() + ", parent=" + parent + ", style=" + style, error);
        }
    }

}
