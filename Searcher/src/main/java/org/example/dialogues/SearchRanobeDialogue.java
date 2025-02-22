package org.example.dialogues;

import org.example.search.SearchQuery;

import java.util.Scanner;

public class SearchRanobeDialogue extends SearchDialogue {
    public SearchRanobeDialogue(Scanner scanner) {
        super(scanner);
    }

    public SearchQuery startDialogue() {
        SearchQuery sq = new SearchQuery();

        readField("Название: ", "name", sq);
        readField("Описание: ", "description", sq);
        readField("Тэги: ", "tags", sq);

        return sq;
    }


}
