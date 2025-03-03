package org.example.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.example.util.DocumentParser;

import java.io.File;
import java.io.IOException;

public class SearchIndexInitializer {
    DocumentParser documentParser;
    private int fileCounter = 0;

    public SearchIndexInitializer() {}

    public Searcher initSearchIndex() {
        documentParser = new DocumentParser();

//        String pathToSave = "D:\\Python\\SearchRanobe\\index\\index.lucene";
        String pathToSave = "..\\index\\index.lucene";
        File f = new File(pathToSave);
        boolean exists = f.exists();

        Searcher searcher = new Searcher(pathToSave);

        if(!exists) {
            searcher.createWriter();
            System.out.println("Index Created");
            String descriptionsDirectoryPath = "..\\json\\description";
            String chaptersDirectoryPath = "..\\json\\chapter";
            saveDocuments(descriptionsDirectoryPath, searcher.getWriter());
            saveDocuments(chaptersDirectoryPath, searcher.getWriter());
            searcher.closeWriter();
        }


        return searcher;
    }

    private void saveDocuments(String directoryPath, IndexWriter writer) {
        File dir = new File(directoryPath);
        for (File file : dir.listFiles()) {
            if (!file.isDirectory()) {
                Document doc = documentParser.createDocument(file);
                if(++fileCounter % 200 == 0) {
                    System.out.println(fileCounter + " documents created");
                }

                try {
                    writer.addDocument(doc);
                } catch (IOException ignored) {

                }
            }
        }
    }
}
