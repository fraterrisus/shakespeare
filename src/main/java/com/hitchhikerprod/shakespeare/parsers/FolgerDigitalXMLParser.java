package com.hitchhikerprod.shakespeare.parsers;

import com.hitchhikerprod.shakespeare.text.Script;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.PrintWriter;
import java.util.ArrayList;

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

    @Override
    public Script parse() {
        Script script = new Script();

        logger.info("Looking for acts");
        ArrayList<Node> acts = new ArrayList<>();
        for (Node n = getRoot().getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() != Node.ELEMENT_NODE) {
                logger.info("Skipping non-element node");
                continue;
            }
            if (! n.getNodeName().equalsIgnoreCase("div1")) {
                logger.info("Skipping element " + n.getNodeName());
                continue;
            }
            logger.info("Parsing element " + n.toString());
            NamedNodeMap attributes = n.getAttributes();
            for (int i=0; i < attributes.getLength(); i++) {
                Node a = attributes.item(i);
                if (a.getNodeType() != Node.ATTRIBUTE_NODE) continue;
                logger.info("Parsing attribute " + a);
                String nodeName = a.getNodeName();
                String nodeValue = a.getNodeValue();
                if (nodeName.equalsIgnoreCase("type")) {
                    if (! nodeValue.equalsIgnoreCase("act")) {
                        logger.severe("div1 attribute 'type' is not 'act'");
                        return null;
                    }
                }
                if (nodeName.equalsIgnoreCase("n")) {
                    logger.info("found Act " + nodeValue);
                    int actNum = Integer.valueOf(nodeValue);
                    acts.ensureCapacity(actNum);
                    acts.add(actNum, n);
                }
            }
        }

        return script;
    }
}
