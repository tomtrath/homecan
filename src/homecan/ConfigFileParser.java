package homecan;

import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class ConfigFileParser {
	
	private Document xmlDocument;

	public ConfigFileParser(String xsdFile, String xmlFile) throws SAXException, IOException, ParserConfigurationException {
		// XML parsing
		DocumentBuilderFactory docBuilderfactory = DocumentBuilderFactory.newInstance();
		docBuilderfactory.setNamespaceAware(true);

		// XSD parsing
		File xsd = new File(xsdFile);
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = schemaFactory.newSchema(xsd);
		docBuilderfactory.setSchema(schema);

		DocumentBuilder builder = docBuilderfactory.newDocumentBuilder();
		// builder.setErrorHandler(new SimpleErrorHandler());

		xmlDocument = builder.parse(xmlFile);
		xmlDocument.getDocumentElement().normalize();
		/*
		// Validation
		javax.xml.validation.Validator validator = schema.newValidator();
		Source source = new DOMSource(xmlDocument);

		validator.setErrorHandler(p.new DraconianErrorHandler());
		validator.validate(source);
		
		*/
	}
	
	public Document getXmlDocument() {
		return xmlDocument;
	}
}
