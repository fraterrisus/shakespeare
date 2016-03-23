package com.hitchhikerprod.shakespeare.parsers;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.PrintWriter;

/**
 * Created by bcordes on 3/22/16.
 */
public class ScriptXMLParser {

    private PrintWriter out;

    ScriptXMLParser(PrintWriter out) {
        this.out = out;
    }

    public void print(Node root) {
        recursivePrint(root, 0);
    }

    private void printIndentation(int indent) {
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

    private void recursivePrint(Node n, int indent) {
        short nodeType = n.getNodeType();
        String nodeName = n.getNodeName();
        String nodeValue = n.getNodeValue();
        if (nodeValue != null) nodeValue = nodeValue.trim();

        if (nodeType == Node.TEXT_NODE && nodeName.equals("#text")) {
            if (nodeValue != null && nodeValue.length() == 0) return;
        }

        printIndentation(indent);
        out.print(typeToString(nodeType));
        if (nodeType != Node.TEXT_NODE) out.print(" " + nodeName);
        if (nodeType == Node.TEXT_NODE || nodeType == Node.ATTRIBUTE_NODE) out.print(" " + nodeValue);
        out.println();

        if (nodeType == Node.ELEMENT_NODE) {
            NamedNodeMap attrs = n.getAttributes();
            for (short i = 0; i < attrs.getLength(); i++)
                recursivePrint(attrs.item(i), indent+1);
        }

        if (nodeValue != null && nodeType != Node.TEXT_NODE && nodeType != Node.ATTRIBUTE_NODE) {
            printIndentation(indent+1);
            out.println(nodeValue);
        }

        if (nodeType != Node.ATTRIBUTE_NODE) {
            for (Node child = n.getFirstChild();
                 child != null;
                 child = child.getNextSibling()) {
                recursivePrint(child, indent + 2);
            }
        }
    }

}
