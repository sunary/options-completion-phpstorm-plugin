package main.com.sunary;

/**
 * Created by sunary on 6/5/17.
 */

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PhpDocCommentParser {
    private static final Pattern paramPattern = Pattern.compile("(?:@param\\s+array\\s+\\$\\w+\\s+\\{([^}]+)\\})|(?:@param[^}\n]+)");
    private static final Pattern optionPattern = Pattern.compile("@(?:var|type)\\s+(\\w+(?:\\|\\w+)*)\\s+\\$(\\w+)\\s+([^\n]*)");
    private static final Pattern enumPattern = Pattern.compile("@enum\\s+\\$(\\w+)\\s+\\{([^}]+)\\}");

    public Map<String, OptionsParam> parse(String comment) {
        int position = 0;
        Map<String, OptionsParam> optionsParams = new HashMap<String, OptionsParam>();
        Matcher matcher = paramPattern.matcher(comment);
        while (matcher.find()) {
            String optionsString = matcher.group(1);
            List<String[]> options = parseOptions(optionsString);
            if (options.size() > 0) {
                optionsParams.put(Integer.toString(position), new OptionsParam(Integer.toString(position), options));

            }
            position++;
        }
        return optionsParams;
    }

    public List<OptionsParam> parseEnum(String comment) {
        List<OptionsParam> optionsParams = new ArrayList<>();
        Matcher optionsMatcher = enumPattern.matcher(comment);

        while (optionsMatcher.find()) {
            String name = optionsMatcher.group(1);
            String values = optionsMatcher.group(2);
            optionsParams.add(new OptionsParam(name,
                    Arrays.asList(values.split(",")).stream().map(s -> new String[]{s.trim()}).collect(Collectors.toList())));
        }
        return optionsParams;
    }

    private List<String[]> parseOptions(String optionsString) {
        List<String[]> options = new ArrayList<>();
        if (optionsString == null) {
            return options;
        }
        Matcher optionsMatcher = optionPattern.matcher(optionsString);

        while (optionsMatcher.find()) {
            String type = optionsMatcher.group(1);
            String name = optionsMatcher.group(2);
            String document = optionsMatcher.group(3);
            options.add(new String[]{name, type, document});
        }
        return options;
    }
}
