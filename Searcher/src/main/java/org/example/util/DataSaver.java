package org.example.util;

import org.apache.lucene.index.IndexableField;
import org.example.search.SearchQuery;

import org.apache.lucene.document.Document;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class DataSaver {
    String filePath = "..\\data.txt";
    public void saveData(SearchQuery query, final List<Document> docs) {
        StringBuilder sb = new StringBuilder();
        for (final Document doc : docs) {
            sb.append(query.toString());
            sb.append("\n");
            for (IndexableField field : doc.getFields()) {
                sb.append(field.name());
                sb.append(": ");
                sb.append(field.stringValue().replace('\n', ' '));
                sb.append(" | ");
            }
            sb.append("\n");
            sb.append("\n");
        }
        String text = sb.toString();

        try {
            Files.write(Paths.get(filePath), text.getBytes(), StandardOpenOption.APPEND);
        }
        catch (IOException e) {
            System.out.println(e);
        }
    }
}
