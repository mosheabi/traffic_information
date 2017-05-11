package monitoring.counters.dynamic;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Alexander Yegorov
 */
public class TemplateFormatter {

    private static final String REGEX = "\\$\\{([^\\:\\}]+(?:\\:[^\\}]*)?)\\}";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    public static String format(String template, Object... args) {
        Matcher m = PATTERN.matcher(template);

        List<String> entries = new ArrayList<>();
        while (m.find()) {
            entries.add(m.group(1));
        }

        if (entries.isEmpty()) {
            return args.length > 0 ? String.format(template, args) : template;
        }

        if (entries.size() != args.length) {
            throw new IllegalArgumentException("Invalid arguments size.");
        }

        String text = template;
        for (int i = 0; i < args.length; ++i) {
            String entry = entries.get(i);
            String defaultValue = null;

            int delimiter = entry.indexOf(":");
            if (delimiter != -1) {
                defaultValue = entry.substring(delimiter + 1);
                entry = entry.substring(0, delimiter);
            }

            String value = args[i] != null ? entry.replace("%", args[i].toString()) : defaultValue;
            if (value == null) {
                throw new IllegalStateException(String.format("No default value for %d argument", i));
            }

            text = text.replaceFirst(REGEX, value);
        }

        return text;
    }
}
