package net.milanaleksic.guitransformer.converters;

import net.milanaleksic.guitransformer.*;
import net.milanaleksic.guitransformer.model.*;
import net.milanaleksic.guitransformer.util.ObjectUtil;

import java.lang.reflect.*;
import java.util.*;

import static net.milanaleksic.guitransformer.util.ObjectUtil.allowOperationOnField;

/**
 * User: Milan Aleksic
 * Date: 1/24/13
 * Time: 2:20 PM
 */
public class FormUpdater {

    public static void updateFormFromModel(final Object model, ModelBindingMetaData modelBindingMetaData) {
        try {
            modelBindingMetaData.setFormIsBeingUpdatedFromModelRightNow(true);
            for (Map.Entry<Field, FieldMapping> binding : modelBindingMetaData.getFieldMapping().entrySet()) {
                final FieldMapping fieldMapping = binding.getValue();
                final Object component = fieldMapping.getComponent();
                allowOperationOnField(binding.getKey(), new ObjectUtil.OperationOnField() {
                    @Override
                    public void operate(Field field) throws ReflectiveOperationException, TransformerException {
                        Object modelValue = field.get(model);
                        Method setterMethod = fieldMapping.getSetterMethod();
                        if (setterMethod == null)
                            return;
                        Object currentValue = fieldMapping.getGetterMethod().invoke(component);
                        if (modelValue != null && modelValue.getClass().isArray()) {
                            if (Arrays.equals((Object[]) modelValue, (Object[]) currentValue))
                                return;
                        } else if (modelValue == null && currentValue == null) {
                            return;
                        } else if (modelValue != null && modelValue.equals(currentValue)) {
                            return;
                        }
                        if (fieldMapping.getBindingType().equals(FieldMapping.BindingType.BY_REFERENCE))
                            setterMethod.invoke(component, modelValue);
                        else
                            setterMethod.invoke(component, modelValue == null ? null : modelValue.toString());
                    }
                });
            }
        } catch (TransformerException e) {
            throw new IllegalStateException("Unexpected error occurred when rebinding model", e);
        } finally {
            modelBindingMetaData.setFormIsBeingUpdatedFromModelRightNow(false);
        }
    }

}
