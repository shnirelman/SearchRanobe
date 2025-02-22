package org.example.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class DocumentParser {
    public DocumentParser() {}

    public Document createDocument(File file) {
        try {
            Scanner scanner = new Scanner(file, "UTF-8");
            String text = scanner.useDelimiter("\\Z").next();
            //System.out.println(text);
            Map<String, Object> result =
                    new ObjectMapper().readValue(text, HashMap.class);
            System.out.println(result.size());

            Document document = new Document();
            FieldType textIndexedType = new FieldType();
            textIndexedType.setStored(true);
            textIndexedType.setIndexOptions(IndexOptions.DOCS);
            textIndexedType.setTokenized(true);


            for(var entry : result.entrySet()) {
//                System.out.println(entry.getKey() + ": " + entry.getValue());
                //System.out.println(entry.getKey());
                //System.out.println(entry.getValue().toString());
                document.add(new Field(entry.getKey(), entry.getValue().toString(), textIndexedType));
            }
            return document;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Document createDocument(String path) {
        return createDocument(new File(path));
    }
}
