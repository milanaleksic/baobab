package net.milanaleksic.baobab.converters.typed;

import com.fasterxml.jackson.databind.JsonNode;
import net.milanaleksic.baobab.TransformerException;
import net.milanaleksic.baobab.providers.ResourceBundleProvider;
import net.milanaleksic.baobab.util.StringUtil;

import javax.inject.Inject;
import java.util.MissingResourceException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Milan Aleksic
 * Date: 4/19/12
 * Time: 3:23 PM
 */
public class StringConverter extends TypedConverter<String> {

    private static final Pattern resourceMessage = Pattern.compile("\\[(.*)\\]");

    @Inject
    private ResourceBundleProvider resourceBundleProvider;

    @Override
    public String getValueFromJson(JsonNode node) {
        String fieldValue = node.asText();
        if (StringUtil.isNullOrEmpty(fieldValue))
            return fieldValue;

        // TODO: this should be done better with state machine instead of regex - to allow multiple replacements of different templates
        Matcher matcher = resourceMessage.matcher(fieldValue);
        if (matcher.find()) {
            try {
                return matcher.replaceAll(resourceBundleProvider.getResourceBundle().getString(matcher.group(1)));
            } catch (MissingResourceException ignored) {
                // ignored
            } catch (Exception e) {
                throw new TransformerException("Problem while searching for the string resource", e);
            }
        }

        return fieldValue;
    }

}
