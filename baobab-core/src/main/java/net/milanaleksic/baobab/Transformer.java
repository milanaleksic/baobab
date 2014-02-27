package net.milanaleksic.baobab;

import net.milanaleksic.baobab.converters.*;
import net.milanaleksic.baobab.providers.ResourceBundleProvider;
import net.milanaleksic.baobab.util.*;
import org.eclipse.swt.widgets.Composite;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.*;
import java.util.ResourceBundle;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 11:35 AM
 */
public class Transformer {

    @Inject
    private ResourceBundleProvider resourceBundleProvider;

    @Inject
    private ObjectConverter objectConverter;

    private boolean doNotCreateModalDialogs = false;

    public TransformationContext createNonManagedForm(String definition) {
        return createFormFromString(definition, null);
    }

    public TransformationContext createFormFromString(String definition, @Nullable final Composite parent) {
        return StreamUtil.loanStringStream(definition, new ReaderLoaner<TransformationContext>() {
            @Override
            public TransformationContext loan(Reader reader) {
                return createContextFromReader(reader, parent, null);
            }
        });
    }

    public TransformationContext fillManagedForm(Object formObject) {
        return createFormFromResource(getFullNameOfResource(formObject), null, formObject);
    }

    public TransformationContext fillManagedForm(Object formObject, @Nullable Composite parent) {
        return createFormFromResource(getFullNameOfResource(formObject), parent, formObject);
    }

    public TransformationContext createFormFromResource(final String formResourceLocation, @Nullable final Composite parent,
                                                        @Nullable final Object formObject) {
        return StreamUtil.loanResourceReader(formResourceLocation, new ReaderLoaner<TransformationContext>() {
            @Override
            public TransformationContext loan(Reader reader) {
                return createContextFromReader(reader, parent, formObject);
            }
        });
    }

    private TransformationContext createContextFromReader(Reader definitionStream, Composite parent, Object formObject) {
        TransformationWorkingContext context = new TransformationWorkingContext();
        mapResourceBundleIfExists(context);
        context.setDoNotCreateModalDialogs(doNotCreateModalDialogs);
        context.setWorkItem(parent);
        context = objectConverter.createHierarchy(formObject, context, definitionStream);
        return context.createTransformationContext();
    }

    private String getFullNameOfResource(Object formObject) {
        String thisClassNameAsResourceLocation = formObject.getClass().getCanonicalName().replaceAll("\\.", "/");
        return "/" + thisClassNameAsResourceLocation + ".gui"; //NON-NLS
    }

    private void mapResourceBundleIfExists(TransformationWorkingContext context) {
        final ResourceBundle resourceBundle = resourceBundleProvider.getResourceBundle();
        if (resourceBundle != null)
            context.mapObject("bundle", resourceBundle); //NON-NLS
    }

    public void setDoNotCreateModalDialogs(boolean doNotCreateModalDialogs) {
        this.doNotCreateModalDialogs = doNotCreateModalDialogs;
    }

    public void updateFormFromModel(Object model, TransformationContext transformationContext) {
        FormUpdater.updateFormFromModel(model, transformationContext.getModelBindingMetaData());
    }

}
