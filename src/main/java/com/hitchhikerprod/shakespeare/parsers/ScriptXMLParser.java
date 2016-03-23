package com.hitchhikerprod.shakespeare.parsers;

import com.hitchhikerprod.shakespeare.text.Script;
import com.sun.istack.internal.Nullable;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.PrintWriter;
import java.util.logging.Logger;

/**
 * Created by bcordes on 3/22/16.
 */
public abstract class ScriptXMLParser {

    private PrintWriter out;
    private Node root;
    protected static Logger logger = Logger.getLogger("ScriptXMLParser");

    protected ScriptXMLParser(PrintWriter out, Node root) {
        this.out = out;
        this.root = root;
    }

    protected Node getRoot() { return root; }

    public abstract Script parse();

    public static void print(PrintWriter out, Node root) {
        recursivePrint(out, root, 0);
    }

    public void print() {
        recursivePrint(out, root, 0);
    }

    private static void printIndentation(PrintWriter out, int indent) {
        for (int i = 0; i < indent; i++) out.print(" ");
    }

    private static String typeToString(short nodeType) {
        switch (nodeType) {
            case Node.ATTRIBUTE_NODE:
                return "ATTR";
            case Node.CDATA_SECTION_NODE:
                return "CDATA";
            case Node.COMMENT_NODE:
                return "CMNT";
            case Node.DOCUMENT_NODE:
                return "DOC";
            case Node.DOCUMENT_FRAGMENT_NODE:
                return "FRAG";
            case Node.DOCUMENT_TYPE_NODE:
                return "DOCTYPE";
            case Node.ELEMENT_NODE:
                return "ELMT";
            case Node.ENTITY_NODE:
                return "ENT";
            case Node.ENTITY_REFERENCE_NODE:
                return "ENTREF";
            case Node.NOTATION_NODE:
                return "NOT";
            case Node.PROCESSING_INSTRUCTION_NODE:
                return "PINST";
            case Node.TEXT_NODE:
                return "TXT";
            default:
                return "UNKN";
        }
    }

    private static void recursivePrint(PrintWriter out, Node n, int indent) {
        short nodeType = n.getNodeType();
        String nodeName = n.getNodeName();
        String nodeValue = n.getNodeValue();
        if (nodeValue != null) nodeValue = nodeValue.trim();

        if (nodeType == Node.TEXT_NODE && nodeName.equals("#text")) {
            if (nodeValue != null && nodeValue.length() == 0) return;
        }

        printIndentation(out, indent);
        out.print(typeToString(nodeType));
        if (nodeType != Node.TEXT_NODE) out.print(" " + nodeName);
        if (nodeType == Node.TEXT_NODE || nodeType == Node.ATTRIBUTE_NODE) out.print(" " + nodeValue);
        out.println();

        if (nodeType == Node.ELEMENT_NODE) {
            NamedNodeMap attrs = n.getAttributes();
            for (short i = 0; i < attrs.getLength(); i++)
                recursivePrint(out, attrs.item(i), indent+1);
        }

        if (nodeValue != null && nodeType != Node.TEXT_NODE && nodeType != Node.ATTRIBUTE_NODE) {
            printIndentation(out, indent+1);
            out.println(nodeValue);
        }

        if (nodeType != Node.ATTRIBUTE_NODE) {
            for (Node child = n.getFirstChild();
                 child != null;
                 child = child.getNextSibling()) {
                recursivePrint(out, child, indent + 2);
            }
        }
    }

    /**
     * Given a root node, searches children of that node for nodes of type ELEMENT with the requested name. If the
     * root node is <code>null</code> or the name is not found, returns <code>null</code>.
     * @param haystack The root node to begin looking
     * @param name The element name to look for (case-insensitive)
     * @return Node if found, or null if not found or root node was null
     */
    protected static Node findElementWithName(@Nullable Node haystack, String name) {
        logger.info("findElementWithName " + name);
        if (haystack == null) {
            logger.info("  null");
            return null;
        }
        for (Node needle = haystack.getFirstChild(); needle != null; needle = needle.getNextSibling()) {
            if (needle.getNodeType() != Node.ELEMENT_NODE) continue;
            if (! needle.getNodeName().equalsIgnoreCase(name)) continue;
            logger.info("  found");
            return needle;
        }
        logger.info("  not found");
        return null;
    }
}
