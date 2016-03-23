package com.hitchhikerprod.shakespeare.parsers;

import org.w3c.dom.Node;

import java.io.PrintWriter;

/**
 * Created by bcordes on 3/22/16.
 */
public class FolgerDigitalXMLParser extends ScriptXMLParser {

    private FolgerDigitalXMLParser(PrintWriter out, Node root) { super(out, root); }

    public static FolgerDigitalXMLParser of(PrintWriter out, Node node) {
        Node root = node;
        root = findElementWithName(root, "text");
        root = findElementWithName(root, "body");
        return (root == null) ? null : new FolgerDigitalXMLParser(out, root);
    }
}
