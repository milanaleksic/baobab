package net.milanaleksic.baobab;

import net.milanaleksic.baobab.converters.*;
import net.milanaleksic.baobab.providers.ResourceBundleProvider;
import net.milanaleksic.baobab.util.StreamUtil;
import org.eclipse.swt.widgets.Composite;

import javax.inject.Inject;
import java.io.Reader;
import java.util.Optional;
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
        return createFormFromString(definition, Optional.empty());
    }

    public TransformationContext createFormFromString(String definition, final Optional<Composite> parent) {
        return StreamUtil.loanStringStream(definition, reader -> createContextFromReader("STRING", reader, parent, Optional.empty()));
    }

    public TransformationContext fillManagedForm(Object formObject) {
        return createFormFromResource(getFullNameOfResource(formObject), Optional.empty(), Optional.of(formObject));
    }

    public TransformationContext fillManagedForm(Object formObject, Composite parent) {
        return createFormFromResource(getFullNameOfResource(formObject), Optional.of(parent), Optional.of(formObject));
    }

    public TransformationContext createFormFromResource(final String formResourceLocation, final Optional<Composite> parent,
                                                        final Optional<Object> formObject) {
        final String unixFormatLocation = formResourceLocation.replaceAll("\\\\", "/");
        return StreamUtil.loanResourceReader(formResourceLocation,
                reader -> createContextFromReader(unixFormatLocation, reader, parent, formObject));
    }

    private TransformationContext createContextFromReader(String formResourceLocation, Reader definitionStream,
                                                          Optional<Composite> parent, Optional<Object> formObject) {
        TransformationWorkingContext context = new TransformationWorkingContext();
        mapResourceBundleIfExists(context);
        context.setDoNotCreateModalDialogs(doNotCreateModalDialogs);
        parent.ifPresent(context::setWorkItem);
        formObject.ifPresent(context::setFormObject);
        context.setFormLocation(getParentLocation(formResourceLocation));
        return objectConverter
                .createHierarchy(context, definitionStream)
                .createTransformationContext();
    }

    private String getParentLocation(String formResourceLocation) {
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
