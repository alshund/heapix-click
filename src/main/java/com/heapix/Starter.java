package com.heapix;

import com.heapix.google.GoogleSheetParser;
import com.heapix.google.GoogleSheetParserImpl;

public class Starter {

    public static void main(String[] args) {

        GoogleSheetParser parser = new GoogleSheetParserImpl();
        parser.parseTable();
    }
}
