package org.example.search;

import java.util.HashMap;
import java.util.Map;

public class SearchQuery {
    private Map<String, String> fields;
    public SearchQuery() {
        fields = new HashMap<String, String>();
    }

    public void addField(String field, String value) {
        fields.put(field, value);
    }

    public Map<String, String> getFields() {
        return fields;
    }
}
