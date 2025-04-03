package org.example;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.example.dialogues.SearchAnythingDialogue;
import org.example.dialogues.SearchDialogue;
import org.example.dialogues.SearchRanobeDialogue;
import org.example.search.SearchIndexInitializer;
import org.example.search.SearchQuery;
import org.example.search.Searcher;
import org.example.util.DataSaver;
import org.example.util.DocumentPrinter;

import java.io.*;
import java.util.*;

public class Main {
    static void showHelp() {
        System.out.println("Выберите действие:");
        System.out.println("1 - поиск новеллы");
        System.out.println("2 - поиск чего-нибудь");
        System.out.println("3 - выход");
        System.out.print("Номер команды: ");
    }

    private static void runDialogue(Searcher searcher,
                                    Scanner scanner,
                                    SearchDialogue dialogue,
                                    DataSaver dataSaver) {
        scanner.nextLine();
        SearchQuery sq = dialogue.startDialogue();
        try {
            List<Document> docs = searcher.search(sq);
            if(docs.isEmpty()) {
                System.out.println("Ничего не найдено");
            } else {
                dataSaver.saveData(sq, docs);
                for (Document doc : docs) {
                    DocumentPrinter.printDocument(doc);
                    //System.out.println("id: " + doc.get("ranobe_id") + "  " + doc.get("name"));
                }
            }
        } catch (ParseException | IOException e) {
            System.out.println("Ничего не найдено");
        }
    }

    public static void main(String[] args) {
        SearchIndexInitializer initializer = new SearchIndexInitializer();
        Searcher searcher = initializer.initSearchIndex();

        Scanner scanner = new Scanner(System.in);

        SearchDialogue searchRanobeDialogue = new SearchRanobeDialogue(scanner);
        SearchDialogue searchAnythingDialogue = new SearchAnythingDialogue(scanner);

        DataSaver dataSaver = new DataSaver();

        while (true) {
            showHelp();

            int choice = -1;
            while(choice < 0 || choice > 3) {
                try {
                    choice = scanner.nextInt();
                } catch (Exception e) {
                    System.out.println("Ошибка ввода! Попробуйте снова:");
                    scanner.nextLine();
                }
            }

            if(choice == 1) {
                runDialogue(searcher, scanner, searchRanobeDialogue, dataSaver);
            } else if(choice == 2) {
                runDialogue(searcher, scanner, searchAnythingDialogue, dataSaver);
            } else if(choice == 3) {
                break;
            }
        }
    }

}