package com.hitchhikerprod.shakespeare.main;

import com.hitchhikerprod.shakespeare.parsers.FolgerDigitalXMLParser;
import com.hitchhikerprod.shakespeare.parsers.PlayShakespeareXMLParser;
import com.hitchhikerprod.shakespeare.parsers.ScriptXMLParser;
import org.w3c.dom.Document;
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
public class Parser {

    private enum Errno {
        NO_ERROR(0x0),
        ARGUMENT_ERROR(0x1),
        FILE_ERROR(0x2),
        PARSE_ERROR(0x4);

        private short code;

        Errno(int code) { this.code = (short)code; }

        public short toShort() { return code; }

        public static short combine(Errno... codes) {
            int reduce = 0x0;
            for (Errno c : codes)
                reduce = reduce | c.toShort();
            return (short)reduce;
        }
    }

    private static PrintWriter out, err;

    private static boolean optPrintMode = false;
    private static String optFilename = null;

    public static void usage(Errno code) {
        out.println("Usage: shakespeare [options] filename.xml");
        out.println();
        out.println("Options:");
        out.println(" -h --help         Display usage");
        out.println(" -p --print        Read the XML file and print the parsed DOM");
        System.exit(code.toShort());
    }

    private static void parseArgs(String[] args) {
        for (String arg : args) {
            if (arg.equals("-h") || arg.equals("--help")) {
                usage(Errno.NO_ERROR);
            }

            if (arg.equals("-p") || arg.equals("--print")) {
                optPrintMode = true;
                continue;
            }

            if (arg.startsWith("-")) {
                System.err.println("Unrecognized switch " + arg);
                usage(Errno.ARGUMENT_ERROR);
            }

            optFilename = arg;
        }

        if (optFilename == null) {
            err.println("No filename specified");
            usage(Errno.ARGUMENT_ERROR);
        }
    }

    // Folger: DOC > ELMT/TEI > ELMT/text > ELMT/body
    // Play: DOC > ELMT/play > ELMT/act [num=1]

    public static void main(String[] args) throws Exception {
        out = new PrintWriter(new OutputStreamWriter(System.out, "UTF-8"), true);
        err = new PrintWriter(new OutputStreamWriter(System.err, "UTF-8"), true);

        parseArgs(args);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setCoalescing(true); // merge CDATA and TEXT nodes
        factory.setExpandEntityReferences(true);
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);

        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new ScriptErrorHandler(err));

        Document root = builder.parse(new File(optFilename));
        if (root.getNodeType() != Node.DOCUMENT_NODE) {
            err.println("Parse error: root document node not found?!");
            System.exit(Errno.FILE_ERROR.toShort());
        }

        ScriptXMLParser parser = null;

        if (optPrintMode) {
            ScriptXMLParser.print(out, root);
            System.exit(Errno.NO_ERROR.toShort());
        }

        Node node = root.getFirstChild();
        while (node != null && node.getNodeType() != Node.ELEMENT_NODE) node = node.getNextSibling();
        if (node == null) {
            err.println("Parse error: couldn't find any elements in the document?!");
            System.exit(Errno.FILE_ERROR.toShort());
        }
        String nodeName = node.getNodeName();
        if (nodeName.equalsIgnoreCase("tei")) {
            err.println("Seems to be a Folger Digital XML file");
            parser = FolgerDigitalXMLParser.of(out, node);
        } else if (nodeName.equalsIgnoreCase("play")) {
            err.println("Seems to be a Play Shakespeare XML file");
            parser = PlayShakespeareXMLParser.of(out, node);
        }
        if (parser == null) {
            err.println("Parse error: didn't recognize the document structure, don't know how to parse it");
            System.exit(Errno.combine(Errno.FILE_ERROR, Errno.PARSE_ERROR));
        }

        parser.print();
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
