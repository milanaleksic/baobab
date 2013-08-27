package net.milanaleksic.baobab.converters;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.milanaleksic.baobab.TransformerException;
import net.milanaleksic.baobab.builders.Builder;
import net.milanaleksic.baobab.builders.BuilderContext;
import net.milanaleksic.baobab.providers.BuilderProvider;
import net.milanaleksic.baobab.providers.ConverterProvider;
import org.codehaus.jackson.JsonNode;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import static net.milanaleksic.baobab.util.ObjectUtil.*;

/**
 * User: Milan Aleksic
 * Date: 2/5/13
 * Time: 2:20 PM
 */
public class ObjectCreationNodeProcessor implements NodeProcessor {

    @Inject
    private ConverterProvider converterProvider;

    @Inject
    private BuilderProvider builderProvider;

    @Inject
    private OldSchoolObjectCreator oldSchoolObjectCreator;

    @Inject
    private ShortHandObjectCreator shortHandObjectCreator;

    @Override
    public TransformationWorkingContext visitHierarchyItem(TransformationWorkingContext context, String key, JsonNode value) {
        if (shortHandObjectCreator.isEligibleForItem(key, value))
            return shortHandObjectCreator.create(context, key, value);
        else if (oldSchoolObjectCreator.isEligibleForItem(key, value))
            return oldSchoolObjectCreator.create(context, key, value);
        throw new TransformerException("No creator eligible for key=" + key + ", value=" + value);
    }

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
            if (ObjectConverter.SPECIAL_KEYS.contains(propertyName))
                return;

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

}
