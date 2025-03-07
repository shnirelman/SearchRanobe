package org.example.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
//import org.apache.lucene.analysis.ru.RussianAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.ru.*;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.synonym.SynonymFilter;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.util.CharsRef;

import java.io.*;
import java.util.Scanner;

public class MyAnalyzer extends Analyzer {
//public class MyAnalyzer{
    private final SynonymMap synonymMap;

    public MyAnalyzer() {
        SynonymMap.Builder builder = new SynonymMap.Builder(true);
        String file = "..\\synonym_dictionary\\synonyms.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                Scanner scanner = new Scanner(line);
                //System.out.println(line);
                String word = scanner.next().toLowerCase();
                String separator = scanner.next();

                while (scanner.hasNext()) {
                    String syn = scanner.next().toLowerCase();
                    builder.add(new CharsRef(word), new CharsRef(syn), true);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            this.synonymMap = builder.build();
        } catch (IOException e) {
            System.out.println("Error reading synonyms from file");
            throw new RuntimeException(e);
        }
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer source = new WhitespaceTokenizer();
        TokenStream result = new LowerCaseFilter(source);

        result = new RussianLightStemFilter(result);
        result = new ShingleFilter(result, 2, 2);
        result = new SynonymGraphFilter(result, synonymMap, true);
        return new TokenStreamComponents(source, result);
    }

    public SynonymMap getSynonymMap() {
        return synonymMap;
    }
}
