package net.milanaleksic.baobab;

import net.milanaleksic.baobab.converters.*;
import net.milanaleksic.baobab.providers.ResourceBundleProvider;
import net.milanaleksic.baobab.util.StreamLoaner;
import net.milanaleksic.baobab.util.StreamUtil;
import org.eclipse.swt.widgets.Shell;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.InputStream;
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

    public TransformationContext createNonManagedForm(@Nullable Shell parent, String definition) {
        return transformFromContent(parent, definition);
    }

    public TransformationContext fillManagedForm(Object formObject) {
        return createFormFromResource(null, formObject, getFullNameOfResource(formObject));
    }

    public TransformationContext fillManagedForm(@Nullable Shell parent, Object formObject) {
        return createFormFromResource(parent, formObject, getFullNameOfResource(formObject));
    }

    public TransformationContext createFormFromResource(@Nullable final Shell parent, @Nullable final Object formObject,
                                                        final String formFileFullName) {
        return StreamUtil.loanResourceStream(formFileFullName, new StreamLoaner<TransformationContext>() {
            @Override
            public TransformationContext loan(InputStream stream) {
                TransformationWorkingContext context = new TransformationWorkingContext(formFileFullName);
                mapResourceBundleIfExists(context);
                context.setDoNotCreateModalDialogs(doNotCreateModalDialogs);
                context.setWorkItem(parent);
                context = objectConverter.createHierarchy(formObject, context, stream);
                return context.createTransformationContext();
            }
        });
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

    private TransformationContext transformFromContent(@Nullable Shell parent, String content) {
        TransformationWorkingContext context = new TransformationWorkingContext();
        mapResourceBundleIfExists(context);
        context.setDoNotCreateModalDialogs(doNotCreateModalDialogs);
        context.setWorkItem(parent);
        context = objectConverter.createHierarchy(context, content);
        return context.createTransformationContext();
    }

    public void setDoNotCreateModalDialogs(boolean doNotCreateModalDialogs) {
        this.doNotCreateModalDialogs = doNotCreateModalDialogs;
    }

    public void updateFormFromModel(Object model, TransformationContext transformationContext) {
        FormUpdater.updateFormFromModel(model, transformationContext.getModelBindingMetaData());
    }

}
