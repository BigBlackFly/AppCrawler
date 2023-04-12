package utils;

import io.appium.java_client.android.AndroidDriver;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class XmlParser {

    public XmlParser() {
    }

    private final SAXReader reader = new SAXReader();

    private final List<String> attributes = Arrays.asList("package", "class", "resource-id");

    public String refine(AndroidDriver driver, String pageSource) throws IOException {
        try (InputStream targetStream = new ByteArrayInputStream(pageSource.getBytes())) {
            Thread.sleep(1000);
            Document currentDocument = reader.read(targetStream);
            String refinedPageSource = XmlTools.filterDocument(currentDocument, this.attributes);
            String pageName = driver.currentActivity();
            return pageName + "\n" + refinedPageSource;
        } catch (DocumentException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
