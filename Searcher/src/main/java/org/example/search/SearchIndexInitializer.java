package org.example.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.example.util.DocumentParser;

import java.io.File;
import java.io.IOException;

public class SearchIndexInitializer {
    DocumentParser documentParser;

    public SearchIndexInitializer() {}

    public Searcher initSearchIndex() {
        documentParser = new DocumentParser();

        String pathToSave = "D:\\Python\\SearchRanobe\\index\\index.lucene";
        File f = new File(pathToSave);
        boolean exists = f.exists();

        Searcher searcher = new Searcher(pathToSave);

        if(!exists) {
            searcher.createWriter();
            String descriptionsDirectoryPath = "D:\\Python\\SearchRanobe\\json\\description";
            String chaptersDirectoryPath = "D:\\Python\\SearchRanobe\\json\\chapter";
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
                System.out.println(file.getName());
                try {
                    writer.addDocument(doc);
                } catch (IOException ignored) {

                }
            }
        }
    }
}
