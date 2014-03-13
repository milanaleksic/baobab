package net.milanaleksic.baobab;

import net.milanaleksic.baobab.converters.*;
import net.milanaleksic.baobab.providers.ResourceBundleProvider;
import net.milanaleksic.baobab.util.StreamUtil;
import org.eclipse.swt.widgets.Composite;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.Reader;
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
        return StreamUtil.loanStringStream(definition, reader -> createContextFromReader(null, reader, parent, null));
    }

    public TransformationContext fillManagedForm(Object formObject) {
        return createFormFromResource(getFullNameOfResource(formObject), null, formObject);
    }

    public TransformationContext fillManagedForm(Object formObject, @Nullable Composite parent) {
        return createFormFromResource(getFullNameOfResource(formObject), parent, formObject);
    }

    public TransformationContext createFormFromResource(final String formResourceLocation, @Nullable final Composite parent,
                                                        @Nullable final Object formObject) {
        final String unixFormatLocation = formResourceLocation == null ? "" : formResourceLocation.replaceAll("\\\\", "/");
        return StreamUtil.loanResourceReader(formResourceLocation,
                reader -> createContextFromReader(unixFormatLocation, reader, parent, formObject));
    }

    private TransformationContext createContextFromReader(String formResourceLocation, Reader definitionStream, Composite parent, Object formObject) {
        TransformationWorkingContext context = new TransformationWorkingContext();
        mapResourceBundleIfExists(context);
        context.setDoNotCreateModalDialogs(doNotCreateModalDialogs);
        context.setWorkItem(parent);
        context.setObjectConverter(objectConverter);
        context.setFormObject(formObject);
        context.setFormLocation(getParentLocation(formResourceLocation));
        return objectConverter
                .createHierarchy(context, definitionStream)
                .createTransformationContext();
    }

    private String getParentLocation(String formResourceLocation) {
        if (formResourceLocation == null)
            return null;
        int lastSlash = formResourceLocation.lastIndexOf('/');
        return lastSlash == -1 ? formResourceLocation : formResourceLocation.substring(0, lastSlash + 1);
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
