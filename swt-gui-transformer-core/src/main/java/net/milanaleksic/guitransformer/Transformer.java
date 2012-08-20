package net.milanaleksic.guitransformer;

import net.milanaleksic.guitransformer.providers.ResourceBundleProvider;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.swt.widgets.Shell;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
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

    @Inject
    private EmbeddingService embeddingService;

    private final ObjectMapper mapper;

    private boolean doNotCreateModalDialogs = false;

    public Transformer() {
        this.mapper = new ObjectMapper();
        this.mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    }

    public TransformationContext createNonManagedForm(@Nullable Shell parent, String definition) throws TransformerException {
        return transformFromContent(parent, definition);
    }

    public TransformationContext fillManagedForm(Object formObject) throws TransformerException {
        return this.fillManagedForm(null, formObject);
    }

    public TransformationContext fillManagedForm(@Nullable Shell parent, Object formObject) throws TransformerException {
        String thisClassNameAsResourceLocation = formObject.getClass().getCanonicalName().replaceAll("\\.", "/");
        String formName = "/" + thisClassNameAsResourceLocation + ".gui"; //NON-NLS

        TransformationContext transformationContext = transformFromResourceName(parent, formName);
        embeddingService.embed(formObject, transformationContext);
        return transformationContext;
    }

    TransformationContext createFormFromResource(String fullName) throws TransformerException {
        return transformFromResourceName(null, fullName);
    }

    private TransformationContext transformFromResourceName(@Nullable Shell parent, String fullName) throws TransformerException {
        TransformationWorkingContext context = new TransformationWorkingContext();
        InputStream resourceAsStream = null;
        try {
            mapResourceBundleIfExists(context);
            context.setDoNotCreateModalDialogs(doNotCreateModalDialogs);
            context.setWorkItem(parent);

            resourceAsStream = Transformer.class.getResourceAsStream(fullName);
            final JsonNode shellDefinition = mapper.readValue(resourceAsStream, JsonNode.class);
            context = objectConverter.createHierarchy(context, shellDefinition);
            return context.createTransformationContext();
        } catch (IOException e) {
            throw new TransformerException("IO Error while trying to find and parse required form: " + fullName, e);
        } finally {
            try {
                if (resourceAsStream != null) resourceAsStream.close();
            } catch (Exception ignored) {
            }
        }
    }

    private void mapResourceBundleIfExists(TransformationWorkingContext context) {
        final ResourceBundle resourceBundle = resourceBundleProvider.getResourceBundle();
        if (resourceBundle != null)
            context.mapObject("bundle", resourceBundle); //NON-NLS
    }

    private TransformationContext transformFromContent(@Nullable Shell parent, String content) throws TransformerException {
        TransformationWorkingContext context = new TransformationWorkingContext();
        try {
            mapResourceBundleIfExists(context);
            context.setDoNotCreateModalDialogs(doNotCreateModalDialogs);
            context.setWorkItem(parent);

            final JsonNode shellDefinition = mapper.readValue(content, JsonNode.class);
            context = objectConverter.createHierarchy(context, shellDefinition);
            return context.createTransformationContext();
        } catch (IOException e) {
            throw new TransformerException("IO Error while trying to parse content: " + content, e);
        }
    }

    public void setDoNotCreateModalDialogs(boolean doNotCreateModalDialogs) {
        this.doNotCreateModalDialogs = doNotCreateModalDialogs;
    }

    public void updateFormFromModel(Object model) {
        embeddingService.updateFormFromModel(model);
    }
}
