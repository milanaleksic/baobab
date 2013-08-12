package net.milanaleksic.guitransformer.integration.loader.impl;

import com.google.common.base.Optional;
import net.milanaleksic.guitransformer.integration.loader.Loader;
import net.milanaleksic.guitransformer.util.ObjectUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.*;

import javax.inject.Inject;
import java.lang.reflect.Field;

/**
 * User: Milan Aleksic
 * Date: 1/23/13
 * Time: 1:29 PM
 */
public class SpringLoader implements Loader, ApplicationContextAware {

    private Optional<ApplicationContext> applicationContext = Optional.absent();

    @Override
    public void load(Object raw) {
        fieldInjectionForObject(ObjectUtil.getFieldsWithAnnotation(raw.getClass(), Inject.class), raw);
    }

    private void fieldInjectionForObject(Iterable<Field> fieldsForInjection, final Object raw) {
        for (Field field : fieldsForInjection)
            ObjectUtil.setFieldValueOnObject(field, raw, getBeanByClass(field.getType()));
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
