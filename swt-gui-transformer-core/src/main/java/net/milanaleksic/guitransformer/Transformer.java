package net.milanaleksic.guitransformer;

import net.milanaleksic.guitransformer.converters.*;
import net.milanaleksic.guitransformer.providers.ResourceBundleProvider;
import org.eclipse.swt.widgets.Shell;

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

    @Inject
    private EmbeddingService embeddingService;

    private boolean doNotCreateModalDialogs = false;

    public TransformationContext createNonManagedForm(@Nullable Shell parent, String definition) throws TransformerException {
        return transformFromContent(parent, definition);
    }

    public TransformationContext fillManagedForm(Object formObject) throws TransformerException {
        return createFormFromResource(null, formObject, getFullNameOfResource(formObject));
    }

    public TransformationContext fillManagedForm(@Nullable Shell parent, Object formObject) throws TransformerException {
        return createFormFromResource(parent, formObject, getFullNameOfResource(formObject));
    }

    public TransformationContext createFormFromResource(@Nullable Shell parent, @Nullable Object formObject, String formFileFullName) throws TransformerException {
        TransformationWorkingContext context = new TransformationWorkingContext(formFileFullName);
        InputStream resourceAsStream = null;
        try {
            mapResourceBundleIfExists(context);
            context.setDoNotCreateModalDialogs(doNotCreateModalDialogs);
            context.setWorkItem(parent);

            resourceAsStream = Transformer.class.getResourceAsStream(formFileFullName);

            context = objectConverter.createHierarchy(context, resourceAsStream);
            if (formObject != null)
                embeddingService.embed(formObject, context);
            return context.createTransformationContext();
        } finally {
            try {
                if (resourceAsStream != null) resourceAsStream.close();
            } catch (Exception ignored) {
            }
        }
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

    private TransformationContext transformFromContent(@Nullable Shell parent, String content) throws TransformerException {
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
        embeddingService.updateFormFromModel(model, transformationContext);
    }

}
