package com.hitchhikerprod.shakespeare.parsers;

import org.w3c.dom.Node;

import java.io.PrintWriter;

/**
 * Created by bcordes on 3/22/16.
 */
public class PlayShakespeareXMLParser extends ScriptXMLParser {
    private Node root;

    public PlayShakespeareXMLParser(PrintWriter out, Node node) {
        super(out);
        this.root = node;
    }
}
