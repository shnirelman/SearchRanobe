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
import org.example.dssm.DSSMClient;

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
    private DSSMClient dssmClient;

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

        dssmClient = new DSSMClient();
    }

    private float combineScores(float luceneScore, float dssmScore) {
        return (float) (luceneScore * 0.02 +  dssmScore * 0.98);
    }

    private class PairFloatDoc {
        float first;
        Document second;

        public PairFloatDoc(float first, Document second) {
            this.first = first;
            this.second = second;
        }

        public float getFirst() {
            return first;
        }

        public void setFirst(float first) {
            this.first = first;
        }

        public Document getSecond() {
            return second;
        }

        public void setSecond(Document second) {
            this.second = second;
        }
    }

    private class PairFloatDocumentComparator implements Comparator<PairFloatDoc> {

        @Override
        public int compare(PairFloatDoc a, PairFloatDoc b) {
            return b.getFirst() - a.getFirst() > 0 ? 1 : -1;
        }
    }


    private int checkDescription(Document document) {
        int ranobe_id = -1;
        boolean res = false;
        for (IndexableField field : document.getFields()) {
            String fieldName = field.name();
            if(fieldName.equals("description")) {
                res = true;
            } else if(fieldName.equals("ranobe_id")) {
                String fieldValue = field.stringValue();
                ranobe_id = Integer.parseInt(fieldValue);
            }
        }
        if(res)
            return ranobe_id;
        else return -1;
    }

    private List<Document> search(Query query, SearchQuery searchQuery) throws ParseException, IOException {
        IndexReader indexReader = DirectoryReader.open(memoryIndex);
        IndexSearcher searcher = new IndexSearcher(indexReader);
        TopDocs topDocs = searcher.search(query, 8000);
        List<Document> documents = new ArrayList<>();

        Map<Integer, Float> dssmScores = dssmClient.getDSSMScores(searchQuery);

        List<PairFloatDoc> pairFloatDocs = new ArrayList<>();

        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document document = searcher.doc(scoreDoc.doc);
            float luceneScore = scoreDoc.score;
            float dssmScore = 0;
            int ranobe_id = checkDescription(document);
            if(ranobe_id != -1) {
                dssmScore = dssmScores.get(ranobe_id);
            }

            float score = combineScores(luceneScore, dssmScore);
            if(ranobe_id != -1 && dssmScore > 0.1) {
                System.out.println("score = " + score + "  dssmScore = " + dssmScore);
            }
            pairFloatDocs.add(new PairFloatDoc(score, document));
        }

        pairFloatDocs.sort(new PairFloatDocumentComparator());

        for(int i = 0; i < 10; i++) {
            documents.add(pairFloatDocs.get(i).getSecond());
            System.out.println(pairFloatDocs.get(i).getFirst());
        }

        return documents;
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

        return search(query, sq);
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
