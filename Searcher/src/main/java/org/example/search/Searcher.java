package org.example.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.xml.builders.FuzzyLikeThisQueryBuilder;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class Searcher {
    public static String ANY_FIELD = "any";
    private Directory memoryIndex;
    private MyAnalyzer analyzer;
//    private Analyzer analyzer;
    private IndexWriterConfig indexWriterConfig;
    private IndexWriter writer;
    private final String[] text_fields;
    private final int maxDistance;

    Searcher(String pathToSave) {
        analyzer = new MyAnalyzer();
        indexWriterConfig = new IndexWriterConfig(analyzer);
        maxDistance = 1;

        //memoryIndex = new RAMDirectory();
        try {
            memoryIndex = FSDirectory.open(Paths.get(pathToSave));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        writer = null;

        text_fields = new String[]{"description", "tags", "name", "text"};
    }

    private List<Document> search(Query query) throws ParseException, IOException {
        IndexReader indexReader = DirectoryReader.open(memoryIndex);
        IndexSearcher searcher = new IndexSearcher(indexReader);
        TopDocs topDocs = searcher.search(query, 10);
        List<Document> documents = new ArrayList<>();
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            documents.add(searcher.doc(scoreDoc.doc));
        }

        return documents;
    }

    public List<Document> searchIndex(String inField, String queryString) throws ParseException, IOException {
        Query query = new QueryParser(inField, analyzer).parse(queryString);

        return search(query);
    }

    public List<Document> search(SearchQuery sq) throws ParseException, IOException {
//        System.out.println("search start");
        ArrayList<Query> queries = new ArrayList<>();
        BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
        for(var entry : sq.getFields().entrySet()) {
//            System.out.println(entry.getKey() + ": " + entry.getValue());
            if(Objects.equals(entry.getKey(), ANY_FIELD)) {
                for(String field : text_fields) {
                    Query query = new QueryParser(field, analyzer).parse(entry.getValue());
                    addFuzzyQuery(booleanQueryBuilder, query);
                }
            } else {
                Query query = new QueryParser(entry.getKey(), analyzer).parse(entry.getValue());
                addFuzzyQuery(booleanQueryBuilder, query);
            }
        }
        Query query = booleanQueryBuilder.build();
        //System.out.println("Query: " + query);

        return search(query);
    }

    void addFuzzyQuery(BooleanQuery.Builder booleanQueryBuilder, Query query) {
        //System.out.println(query.toString());
        if(query instanceof BooleanQuery) {
            for(var clause : ((BooleanQuery) query).clauses()) {
                Query clauseQuery = clause.getQuery();
                addFuzzyQuery(booleanQueryBuilder, clauseQuery);
//                if(clauseQuery instanceof SynonymQuery synonymQuery) {
//                    List<Term> terms = synonymQuery.getTerms();
//                    for(Term term : terms) {
//                        FuzzyQuery fuzzyQuery = new FuzzyQuery(term, maxDistance);
//                        booleanQueryBuilder.add(fuzzyQuery, BooleanClause.Occur.SHOULD);
//                    }
//                } else {
//                    Term term = ((TermQuery)clause.getQuery()).getTerm();
//                    FuzzyQuery fuzzyQuery = new FuzzyQuery(term, maxDistance);
//                    booleanQueryBuilder.add(fuzzyQuery, BooleanClause.Occur.SHOULD);
//                }
            }
        } else {
//            System.out.println(query.getClass());
            if(query instanceof SynonymQuery synonymQuery) {
                List<Term> terms = synonymQuery.getTerms();
                for(Term term : terms) {
                    FuzzyQuery fuzzyQuery = new FuzzyQuery(term, maxDistance);
                    booleanQueryBuilder.add(fuzzyQuery, BooleanClause.Occur.SHOULD);
                }
            } else {
                Term term = ((TermQuery)query).getTerm();
                FuzzyQuery fuzzyQuery = new FuzzyQuery(term, maxDistance);
                booleanQueryBuilder.add(fuzzyQuery, BooleanClause.Occur.SHOULD);
            }
        }
    }

    public void createWriter() {
        try {
            writer = new IndexWriter(memoryIndex, indexWriterConfig);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void closeWriter() {
        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Directory getMemoryIndex() {
        return memoryIndex;
    }

    public void setMemoryIndex(Directory memoryIndex) {
        this.memoryIndex = memoryIndex;
    }

    public MyAnalyzer getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(MyAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    public IndexWriterConfig getIndexWriterConfig() {
        return indexWriterConfig;
    }

    public void setIndexWriterConfig(IndexWriterConfig indexWriterConfig) {
        this.indexWriterConfig = indexWriterConfig;
    }

    public IndexWriter getWriter() {
        return writer;
    }

    public void setWriter(IndexWriter writer) {
        this.writer = writer;
    }

}
