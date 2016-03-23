package com.hitchhikerprod.shakespeare.parsers;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Created by bcordes on 3/22/16.
 */
public class ScriptXMLParser {

    private static PrintWriter out, err;

    public static void usage(int code) {
        out.println("Usage: shakespeare [options] filename.xml");
        System.exit(code);
    }

    private static String parseArgs(String[] args) {
        String filename = null;

        for (String arg : args) {
            if (arg.startsWith("--")) {
                if (arg.equals("--help")) {
                    usage(0);
                } else {
                    System.err.println("Unrecognized switch " + arg);
                    usage(1);
                }
                continue;
            }
            if (arg.startsWith("-")) {
                if (arg.equals("-h")) {
                    usage(0);
                } else {
                    System.err.println("Unrecognized switch " + arg);
                    usage(1);
                }
                continue;
            }

            filename = arg;
        }

        if (filename == null) { usage(1); }
        return filename;
    }

    public static String typeToString(short nodeType) {
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

    private static void printIndentation(int indent) {
        for (short i = 0; i < indent; i++) out.print(" ");
    }

    private static void recursivePrint(Node n, int indent) {
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

    // Folger: DOC > ELMT/TEI > ELMT/text > ELMT/body
    // Play: DOC > ELMT/play > ELMT/act [num=1]

    public static void main(String[] args) throws Exception {
        out = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"), true);
        err = new PrintWriter(new OutputStreamWriter(System.err, "UTF-8"), true);

        String filename = parseArgs(args);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setCoalescing(true); // merge CDATA and TEXT nodes
        factory.setExpandEntityReferences(true);
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new ScriptErrorHandler(err));

        Document root = builder.parse(new File(filename));
        if (root.getNodeType() != Node.DOCUMENT_NODE) {
            err.println("Parse error: root document node not found?!");
            System.exit(2);
        }

        recursivePrint(root, 0);
    }

    private static class ScriptErrorHandler implements ErrorHandler {
        private PrintWriter out;

        ScriptErrorHandler(PrintWriter out) {
            this.out = out;
        }

        private String getParseExceptionInfo(SAXParseException spe) {
            String systemId = spe.getSystemId();
            if (systemId == null) {
                systemId = "null";
            }

            String info = "URI=" + systemId + " Line=" + spe.getLineNumber() +
                          ": " + spe.getMessage();
            return info;
        }

        public void warning(SAXParseException spe) throws SAXException {
            out.println("Warning: " + getParseExceptionInfo(spe));
        }

        public void error(SAXParseException spe) throws SAXException {
            String message = "Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }

        public void fatalError(SAXParseException spe) throws SAXException {
            String message = "Fatal Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }
    }
}
