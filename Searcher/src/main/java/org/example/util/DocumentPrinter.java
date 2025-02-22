package org.example.util;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;

public class DocumentPrinter {
    public static void printDocument(final Document document) {
        for (IndexableField field : document.getFields()) {
            String fieldName = field.name();
            if(fieldName.equals("other_names")) {
                continue;
            }

            String fieldValue = field.stringValue();

            if(!fieldName.equals("name") && !fieldName.equals("description") && fieldValue.length() > 60) {
                fieldValue = fieldValue.substring(0, 60) + "...";
            }
            System.out.println(fieldName + ": " +
                    fieldValue.replaceAll("[\\r\\n]", "").replaceAll("\\s+", " "));
        }
        System.out.println();
    }
}
