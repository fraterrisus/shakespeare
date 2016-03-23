package com.hitchhikerprod.shakespeare.parsers;

import com.hitchhikerprod.shakespeare.text.Script;
import org.w3c.dom.Node;

import java.io.PrintWriter;

/**
 * Created by bcordes on 3/22/16.
 */
public class PlayShakespeareXMLParser extends ScriptXMLParser {

    private PlayShakespeareXMLParser(PrintWriter out, Node root) { super(out, root); }

    public static PlayShakespeareXMLParser of(PrintWriter out, Node node) {
        Node root = node;
        root = findElementWithName(root, "play");
        return (root == null) ? null : new PlayShakespeareXMLParser(out, root);
    }

    public Script parse() {
        Script script = new Script();
        return script;
    }
}
