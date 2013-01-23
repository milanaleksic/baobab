package net.milanaleksic.guitransformer.loader.impl;

import com.google.common.base.*;
import com.google.common.collect.Iterables;
import net.milanaleksic.guitransformer.TransformerException;
import net.milanaleksic.guitransformer.loader.Loader;
import net.milanaleksic.guitransformer.util.ObjectUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.*;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * User: Milan Aleksic
 * Date: 1/23/13
 * Time: 1:29 PM
 */
public class SpringLoader implements Loader, ApplicationContextAware {

    private Optional<ApplicationContext> applicationContext = Optional.absent();

    @Override
    public void load(Object raw) throws TransformerException {
        final Iterable<Field> fieldsForInjection = Iterables.filter(Arrays.asList(raw.getClass().getDeclaredFields()), new Predicate<Field>() {
            @Override
            public boolean apply(Field input) {
                return input.getAnnotation(Inject.class) != null;
            }
        });
        fieldInjectionForObject(fieldsForInjection, raw);
    }

    private void fieldInjectionForObject(Iterable<Field> fieldsForInjection, final Object raw) throws TransformerException {
        for(Field field : fieldsForInjection) {
            ObjectUtil.allowOperationOnField(field, new ObjectUtil.OperationOnField() {
                @Override
                public void operate(Field field) throws ReflectiveOperationException, TransformerException {
                    field.set(raw, getBeanByClass(field.getType()));
                }
            });
        }
    }

    private Object getBeanByClass(Class<?> clazz) {
        try {
            return applicationContext.get().getBean(clazz);
        } catch (NoSuchBeanDefinitionException e) {
            throw new RuntimeException("Bean could not be found: " + clazz.getName(), e);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = Optional.of(applicationContext);
    }

}
