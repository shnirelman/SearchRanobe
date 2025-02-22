package org.example.dialogues;

import org.example.search.SearchQuery;
import org.example.search.Searcher;

import java.util.Scanner;

public class SearchAnythingDialogue  extends SearchDialogue {
    public SearchAnythingDialogue(Scanner scanner) {
        super(scanner);
    }

    public SearchQuery startDialogue() {
        SearchQuery sq = new SearchQuery();

        readField("Введите запрос: ", Searcher.ANY_FIELD, sq);

        return sq;
    }
}
