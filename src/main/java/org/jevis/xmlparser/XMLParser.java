/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.xmlparser;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisType;
import org.jevis.commons.DatabaseHelper;
import org.jevis.commons.driver.Converter;
import org.jevis.commons.driver.ConverterFactory;
import org.jevis.commons.driver.DataCollectorTypes;
import org.jevis.commons.driver.Parser;
import org.jevis.commons.driver.Result;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author bf
 */
public class XMLParser implements Parser {

//        private List<XMLDatapointParser> _datapointParsers = new ArrayList<XMLDatapointParser>();
    private String _dateFormat;
    private String _timeFormat;
    private String _decimalSeperator;
    private String _thousandSeperator;
    private String _mainElement;
    private String _mainAttribute;
    private String _valueElement;
    private String _valueAtribute;
    private Boolean _valueInElement;
    private String _dateElement;
    private String _dateAttribute;
    private Boolean _dateInElement;

    private List<JEVisObject> _dataPoints = new ArrayList<JEVisObject>();
    private List<Result> _results = new ArrayList<Result>();
    private Converter _converter;

    @Override
    public void parse(List<InputStream> input) {
        System.out.println("XMl File Parsing starts");
        for (InputStream inputStream : input) {

            _converter.convertInput(inputStream);
            List<Document> documents = (List<Document>) _converter.getConvertedInput(Document.class);

            for (Document d : documents) {
                NodeList elementsByTagName = d.getElementsByTagName(_mainElement);

                DOMSource domSource = new DOMSource(d);
                StringWriter writer = new StringWriter();
                StreamResult result = new StreamResult(writer);
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer transformer;
                try {
                    transformer = tf.newTransformer();
                    transformer.transform(domSource, result);
                } catch (TransformerConfigurationException ex) {
                    Logger.getLogger(XMLParser.class.getName()).log(Level.ERROR, null, ex);
                } catch (TransformerException ex) {
                    Logger.getLogger(XMLParser.class.getName()).log(Level.ERROR, null, ex);
                }
                System.out.println(writer.toString());

                //iterate over all nodes with the element name
                for (int i = 0; i < elementsByTagName.getLength(); i++) {
                    Node currentNode = elementsByTagName.item(i);
                    Node mainAttributeNode = null;
                    if (_mainAttribute != null) {
                        NamedNodeMap attributes = currentNode.getAttributes();
                        mainAttributeNode = attributes.getNamedItem(_mainAttribute);
                        if (mainAttributeNode == null) {
                            continue;
                        }
                    }
//                    ic.setXMLInput(currentNode);

                    //single parsing
                    boolean isCorrectNode = true; //eigentl false
                    DateTime dateTime = null;
                    Double value = null;
                    Long datapoint = null;
                    try {
                        parseNode(currentNode, mainAttributeNode);
                    } catch (JEVisException ex) {
                        java.util.logging.Logger.getLogger(XMLParser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                    }

                    //parse the correct node
                    if (isCorrectNode) {
                    }
                }
            }
        }
    }

    private void parseNode(Node currentNode, Node mainAttributeNode) throws JEVisException {

        for (JEVisObject dp : _dataPoints) {
            try {
                JEVisClass dpClass = dp.getJEVisClass();

                JEVisType mappingIdentifierType = dpClass.getType(DataCollectorTypes.DataPoint.XMLDataPoint.MAPPING_IDENTIFIER);
                JEVisType targetType = dpClass.getType(DataCollectorTypes.DataPoint.XMLDataPoint.TARGET);
                JEVisType valueIdentifierType = dpClass.getType(DataCollectorTypes.DataPoint.XMLDataPoint.VALUE_INDEX);

                Long datapointID = dp.getID();
                String mappingIdentifier = DatabaseHelper.getObjectAsString(dp, mappingIdentifierType);
                String targetString = DatabaseHelper.getObjectAsString(dp, targetType);
                Long target = null;
                try {
                    target = Long.parseLong(targetString);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                String valueIdentifier = DatabaseHelper.getObjectAsString(dp, valueIdentifierType);

                if (mainAttributeNode != null && !mainAttributeNode.getNodeValue().equals(valueIdentifier)) {
                    continue;
                }

                boolean correct = false;
                //get Date
                Node dateNode = null;
                if (_dateElement != null) {
                    for (int j = 0; j < currentNode.getChildNodes().getLength(); j++) {
                        Node item = currentNode.getChildNodes().item(j);
                        if (item.getNodeName().equals(_dateElement)) {
                            dateNode = item;
                            break;
                        }
                    }
                } else {
                    dateNode = currentNode.cloneNode(true);
                }
                String dateString = null;
                if (_dateAttribute != null) {
                    Node namedItem = dateNode.getAttributes().getNamedItem(_dateAttribute);
                    dateString = namedItem.getNodeValue();
                } else {
                    dateString = dateNode.getTextContent();
                }
                String pattern = _dateFormat;

                DateTimeFormatter fmt = DateTimeFormat.forPattern(pattern);
                DateTime dateTime = fmt.parseDateTime(dateString);

//                    dpParser.parse(ic);
//                    value = dpParser.getValue();
                //get value
                Node valueNode = null;
                if (_valueElement != null) {
                    for (int j = 0; j < currentNode.getChildNodes().getLength(); j++) {
                        Node item = currentNode.getChildNodes().item(j);
                        if (item.getNodeName().equals(_valueElement)) {
                            valueNode = item;
                            break;
                        }
                    }
                } else {
                    valueNode = currentNode.cloneNode(true);
                }
                String valueString = null;
                if (_valueAtribute != null) {
                    Node namedItem = valueNode.getAttributes().getNamedItem(_valueAtribute);
                    valueString = namedItem.getNodeValue();
                } else {
                    valueString = valueNode.getTextContent();
                }
                Double value = Double.parseDouble(valueString);
                correct = true;

//                    if (dpParser.outOfBounce()) {
//                        org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.WARN, "Date for value out of bounce: " + dateTime);
//                        org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.WARN, "Value out of bounce: " + value);
//                    }
                if (!correct) {
                    continue;
                }
                _results.add(new Result(target, value, dateTime));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public List<Result> getResult() {
        return _results;
    }

    @Override
    public void initialize(JEVisObject parserObject
    ) {
        initializeAttributes(parserObject);

        _converter = ConverterFactory.getConverter(parserObject);

        initializeXMLDataPointParser(parserObject);
    }

    private void initializeAttributes(JEVisObject parserObject) {
        try {
            JEVisClass jeClass = parserObject.getJEVisClass();

            JEVisType dateFormatType = jeClass.getType(DataCollectorTypes.Parser.XMLParser.DATE_FORMAT);
            JEVisType timeFormatType = jeClass.getType(DataCollectorTypes.Parser.XMLParser.TIME_FORMAT);
            JEVisType decimalSeperatorType = jeClass.getType(DataCollectorTypes.Parser.XMLParser.DECIMAL_SEPERATOR);
            JEVisType thousandSeperatorType = jeClass.getType(DataCollectorTypes.Parser.XMLParser.THOUSAND_SEPERATOR);

            JEVisType mainElementType = jeClass.getType(DataCollectorTypes.Parser.XMLParser.XMLParser.MAIN_ELEMENT);
            JEVisType mainAttributeType = jeClass.getType(DataCollectorTypes.Parser.XMLParser.XMLParser.MAIN_ATTRIBUTE);
            JEVisType valueElementType = jeClass.getType(DataCollectorTypes.Parser.XMLParser.XMLParser.VALUE_ELEMENT);
            JEVisType valueAttributeType = jeClass.getType(DataCollectorTypes.Parser.XMLParser.XMLParser.VALUE_ATTRIBUTE);
            JEVisType valueInElement = jeClass.getType(DataCollectorTypes.Parser.XMLParser.XMLParser.VALUE_IN_ELEMENT);
            JEVisType dateElementType = jeClass.getType(DataCollectorTypes.Parser.XMLParser.XMLParser.DATE_ELEMENT);
            JEVisType dateAttributeType = jeClass.getType(DataCollectorTypes.Parser.XMLParser.XMLParser.DATE_ATTRIBUTE);
            JEVisType dateInElement = jeClass.getType(DataCollectorTypes.Parser.XMLParser.XMLParser.DATE_IN_ELEMENT);

            _dateFormat = DatabaseHelper.getObjectAsString(parserObject, dateFormatType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "DateFormat: " + _dateFormat);
            _timeFormat = DatabaseHelper.getObjectAsString(parserObject, timeFormatType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "TimeFormat: " + _timeFormat);
            _decimalSeperator = DatabaseHelper.getObjectAsString(parserObject, decimalSeperatorType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "DecimalSeperator: " + _decimalSeperator);
            _thousandSeperator = DatabaseHelper.getObjectAsString(parserObject, thousandSeperatorType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "ThousandSeperator: " + _thousandSeperator);

            _mainElement = DatabaseHelper.getObjectAsString(parserObject, mainElementType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "MainElement: " + _mainElement);
            _mainAttribute = DatabaseHelper.getObjectAsString(parserObject, mainAttributeType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "MainAttribute: " + _mainAttribute);
            _valueElement = DatabaseHelper.getObjectAsString(parserObject, valueElementType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "ValueElement: " + _valueElement);
            _valueAtribute = DatabaseHelper.getObjectAsString(parserObject, valueAttributeType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "ValueAttribute: " + _valueAtribute);
            _valueInElement = DatabaseHelper.getObjectAsBoolean(parserObject, valueInElement);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "ValueInElement: " + _valueInElement);
            _dateElement = DatabaseHelper.getObjectAsString(parserObject, dateElementType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "DateElement: " + _dateElement);
            _dateAttribute = DatabaseHelper.getObjectAsString(parserObject, dateAttributeType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "DateAttribute: " + _dateAttribute);
            _dateInElement = DatabaseHelper.getObjectAsBoolean(parserObject, dateInElement);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "DateInElement: " + _dateInElement);
        } catch (JEVisException ex) {
            java.util.logging.Logger.getLogger(XMLParser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    private void initializeXMLDataPointParser(JEVisObject parserObject) {
        try {
            JEVisClass dirClass = parserObject.getDataSource().getJEVisClass(DataCollectorTypes.DataPointDirectory.XMLDataPointDirectory.NAME);
            JEVisObject dir = parserObject.getChildren(dirClass, true).get(0);
            JEVisClass dpClass = parserObject.getDataSource().getJEVisClass(DataCollectorTypes.DataPoint.XMLDataPoint.NAME);
            _dataPoints = dir.getChildren(dpClass, true);
        } catch (JEVisException ex) {
            java.util.logging.Logger.getLogger(XMLParser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

}
