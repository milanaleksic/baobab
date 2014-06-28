package net.milanaleksic.baobab.converters;

import com.fasterxml.jackson.databind.JsonNode;
import net.milanaleksic.baobab.TransformerException;
import net.milanaleksic.baobab.builders.Builder;
import net.milanaleksic.baobab.builders.BuilderContext;
import net.milanaleksic.baobab.providers.BuilderProvider;
import net.milanaleksic.baobab.providers.ConverterProvider;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private ShortHandObjectCreator shortHandObjectCreator;

    @Override
    public TransformationWorkingContext visitHierarchyItem(TransformationWorkingContext context, Optional<String> key, JsonNode value) {
        if (shortHandObjectCreator.isEligibleForItem(key, value))
            return shortHandObjectCreator.create(context, key, value);
        throw new TransformerException("No creator eligible for key=" + key + ", value=" + value);
    }

    @Override
    public BuilderContext<?> visitBuilderNotationItem(TransformationWorkingContext context, String builderName, String parameters) {
        List<String> params = Arrays.asList(parameters.split(",")).stream()
                .map(String::trim).collect(Collectors.toList());
        final Builder<?> builder = builderProvider.provideBuilderForName(builderName);
        if (builder == null)
            throw new TransformerException("Builder is not registered: " + builderName);
        return builder.create(context, params);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void visitSingleField(TransformationWorkingContext context, String propertyName, JsonNode propertyNode) {
        try {
            if (ObjectConverter.SPECIAL_KEYS.contains(propertyName))
                return;

            Optional<Method> setterMethod = getSetterByName(context.getWorkItem(), getSetterForField(propertyName));
            if (setterMethod.isPresent()) {
                Method method = setterMethod.get();
                Class<?> argType = method.getParameterTypes()[0];
                Converter converter = converterProvider.provideConverterForClass(argType);
                Object value = converter.getValueFromJson(context, propertyNode);
                method.invoke(context.getWorkItem(), value);
            } else {
                Field field = getFieldByName(context.getWorkItem(), propertyName)
                        .orElseThrow(() -> new TransformerException("No setter nor field " + propertyName +
                                " could be found in class " + context.getWorkItem().getClass().getName() + "; context: " + propertyNode));
                Converter converter = converterProvider.provideConverterForClass(field.getType());
                Object value = converter.getValueFromJson(context, propertyNode);
                field.set(context.getWorkItem(), value);
            }
        } catch (TransformerException e) {
            throw e;
        } catch (Throwable t) {
            throw new TransformerException("Transformation was not successful", t);
        }
    }

}
