/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.xmlparser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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

/**
 *
 * @author bf
 */
public class JEVisXMLParser implements Parser {

    
    private XMLParser _xmlParser;

    /**
     *
     * @param inputList
     */
    @Override
    public void parse(List<InputStream> inputList) {
        _xmlParser.parse(inputList);
    }

    @Override
    public List<Result> getResult() {
        return _xmlParser.getResult();
    }

    @Override
    public void initialize(JEVisObject parserObject) {
        initializeAttributes(parserObject);

        Converter converter = ConverterFactory.getConverter(parserObject);
        _xmlParser.setConverter(converter);

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
            JEVisType valueInElementType = jeClass.getType(DataCollectorTypes.Parser.XMLParser.XMLParser.VALUE_IN_ELEMENT);
            JEVisType dateElementType = jeClass.getType(DataCollectorTypes.Parser.XMLParser.XMLParser.DATE_ELEMENT);
            JEVisType dateAttributeType = jeClass.getType(DataCollectorTypes.Parser.XMLParser.XMLParser.DATE_ATTRIBUTE);
            JEVisType dateInElementType = jeClass.getType(DataCollectorTypes.Parser.XMLParser.XMLParser.DATE_IN_ELEMENT);

            String dateFormat = DatabaseHelper.getObjectAsString(parserObject, dateFormatType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "DateFormat: " + dateFormat);
            String timeFormat = DatabaseHelper.getObjectAsString(parserObject, timeFormatType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "TimeFormat: " + timeFormat);
            String decimalSeperator = DatabaseHelper.getObjectAsString(parserObject, decimalSeperatorType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "DecimalSeperator: " + decimalSeperator);
            String thousandSeperator = DatabaseHelper.getObjectAsString(parserObject, thousandSeperatorType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "ThousandSeperator: " + thousandSeperator);

            String mainElement = DatabaseHelper.getObjectAsString(parserObject, mainElementType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "MainElement: " + mainElement);
            String mainAttribute = DatabaseHelper.getObjectAsString(parserObject, mainAttributeType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "MainAttribute: " + mainAttribute);
            String valueElement = DatabaseHelper.getObjectAsString(parserObject, valueElementType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "ValueElement: " + valueElement);
            String valueAtribute = DatabaseHelper.getObjectAsString(parserObject, valueAttributeType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "ValueAttribute: " + valueAtribute);
            Boolean valueInElement = DatabaseHelper.getObjectAsBoolean(parserObject, valueInElementType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "ValueInElement: " + valueInElement);
            String dateElement = DatabaseHelper.getObjectAsString(parserObject, dateElementType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "DateElement: " + dateElement);
            String dateAttribute = DatabaseHelper.getObjectAsString(parserObject, dateAttributeType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "DateAttribute: " + dateAttribute);
            Boolean dateInElement = DatabaseHelper.getObjectAsBoolean(parserObject, dateInElementType);
            org.apache.log4j.Logger.getLogger(this.getClass().getName()).log(org.apache.log4j.Level.ALL, "DateInElement: " + dateInElement);

            _xmlParser = new XMLParser();
            _xmlParser.setDateFormat(dateFormat);
            _xmlParser.setTimeFormat(timeFormat);
            _xmlParser.setDecimalSeperator(decimalSeperator);
            _xmlParser.setThousandSeperator(thousandSeperator);
            _xmlParser.setMainElement(mainElement);
            _xmlParser.setMainAttribute(mainAttribute);
            _xmlParser.setValueElement(valueElement);
            _xmlParser.setValueAtribute(valueAtribute);
            _xmlParser.setValueInElement(valueInElement);
            _xmlParser.setDateElement(dateElement);
            _xmlParser.setDateAttribute(dateAttribute);
            _xmlParser.setDateInElement(dateInElement);
        } catch (JEVisException ex) {
            java.util.logging.Logger.getLogger(XMLParser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

    private void initializeXMLDataPointParser(JEVisObject parserObject) {
        try {
            JEVisClass dirClass = parserObject.getDataSource().getJEVisClass(DataCollectorTypes.DataPointDirectory.XMLDataPointDirectory.NAME);
            JEVisObject dir = parserObject.getChildren(dirClass, true).get(0);
            JEVisClass dpClass = parserObject.getDataSource().getJEVisClass(DataCollectorTypes.DataPoint.XMLDataPoint.NAME);
            List<JEVisObject> dataPoints = dir.getChildren(dpClass, true);
            List<DataPoint> xmldatapoints = new ArrayList<DataPoint>();
            for (JEVisObject dp : dataPoints) {
                JEVisType mappingIdentifierType = dpClass.getType(DataCollectorTypes.DataPoint.XMLDataPoint.MAPPING_IDENTIFIER);
                JEVisType targetType = dpClass.getType(DataCollectorTypes.DataPoint.XMLDataPoint.TARGET);
                JEVisType valueIdentifierType = dpClass.getType(DataCollectorTypes.DataPoint.XMLDataPoint.VALUE_IDENTIFIER);

                Long datapointID = dp.getID();
                String mappingIdentifier = DatabaseHelper.getObjectAsString(dp, mappingIdentifierType);
                String targetString = DatabaseHelper.getObjectAsString(dp, targetType);
                Long target = null;
                try {
                    target = Long.parseLong(targetString);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                String valueIdent = DatabaseHelper.getObjectAsString(dp, valueIdentifierType);

                DataPoint xmldp = new DataPoint();
                xmldp.setMappingIdentifier(mappingIdentifier);
                xmldp.setTarget(target);
                xmldp.setValueIdentifier(valueIdent);
                xmldatapoints.add(xmldp);
            }
            _xmlParser.setDataPoints(xmldatapoints);
        } catch (JEVisException ex) {
            java.util.logging.Logger.getLogger(JEVisXMLParser.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    }

}
