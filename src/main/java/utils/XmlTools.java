package utils;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.Iterator;
import java.util.List;


public class XmlTools {

    public static String filterDocument(Document document, List<String> attributes) {
        Element root = document.getRootElement();
        processChild(root, attributes);
        return document.asXML();
    }

    private static void processChild(Element element, List<String> attributes) {
        List<Element> elements = element.elements();
        for (Element child : elements) {

            for (Iterator<Attribute> attributeIterator = child.attributeIterator();
                 attributeIterator.hasNext(); ) {
                Attribute item = attributeIterator.next();
                if (!contains(attributes, item.getName())) {
                    attributeIterator.remove();
                    item.detach();
                }
            }

            processChild(child, attributes);
        }
    }

    private static boolean contains(List<String> attributes, String targetAttribute) {
        for (String item : attributes) {
            if (item.equals(targetAttribute)) {
                return true;
            }
        }
        return false;
    }
}
