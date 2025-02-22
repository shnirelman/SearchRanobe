package org.example.dialogues;

import org.example.search.SearchQuery;

import java.util.Scanner;

public abstract class SearchDialogue {
    Scanner scanner;

    public SearchDialogue(Scanner scanner) {
        this.scanner = scanner;
    }

    protected void readField(String Message, String fieldName, SearchQuery sq) {
        System.out.print(Message);
        String value = scanner.nextLine();
        if(!value.isEmpty())
            sq.addField(fieldName, value);
    }

    public abstract SearchQuery startDialogue();
}
