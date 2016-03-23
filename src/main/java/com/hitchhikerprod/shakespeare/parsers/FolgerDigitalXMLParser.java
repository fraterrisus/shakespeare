package com.hitchhikerprod.shakespeare.parsers;

import org.w3c.dom.Node;

import java.io.PrintWriter;

/**
 * Created by bcordes on 3/22/16.
 */
public class FolgerDigitalXMLParser extends ScriptXMLParser {
    private Node root;

    public FolgerDigitalXMLParser(PrintWriter out, Node node) {
        super(out);
        this.root = node;
    }
}
