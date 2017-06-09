package main.com.sunary;

/**
 * Created by sunary on 6/5/17.
 */

import java.util.List;

public class OptionsParam {
    private final String key;
    private final List<String[]> options;

    public OptionsParam(String key, List<String[]> options) {
        this.key = key;
        this.options = options;
    }

    public String getKey(){
        return this.key;
    }

    public List<String[]> getOptions() {
        return options;
    }

}
