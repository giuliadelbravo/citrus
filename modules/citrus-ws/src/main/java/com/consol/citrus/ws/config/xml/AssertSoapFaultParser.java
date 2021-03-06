/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.ws.config.xml;

import java.util.Map;

import org.apache.xerces.util.DOMUtil;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.consol.citrus.config.TestActionRegistry;
import com.consol.citrus.config.util.BeanDefinitionParserUtils;
import com.consol.citrus.config.xml.DescriptionElementParser;
import com.consol.citrus.util.FileUtils;
import com.consol.citrus.ws.actions.AssertSoapFault;

/**
 * Parser for SOAP fault assert action.
 * 
 * @author Christoph Deppisch
 */
public class AssertSoapFaultParser implements BeanDefinitionParser {

    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder beanDefinition;

        beanDefinition = BeanDefinitionBuilder.rootBeanDefinition(AssertSoapFault.class);

        beanDefinition.addPropertyValue("name", element.getLocalName());
        DescriptionElementParser.doParse(element, beanDefinition);

        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("fault-code"), "faultCode");
        BeanDefinitionParserUtils.setPropertyValue(beanDefinition, element.getAttribute("fault-string"), "faultString");
        
        Element faultDetailElement = DomUtils.getChildElementByTagName(element, "fault-detail");
        if (faultDetailElement != null) {
            if (faultDetailElement.hasAttribute("file")) {
                if (StringUtils.hasText(DomUtils.getTextValue(faultDetailElement).trim())) {
                    throw new BeanCreationException("You tried to set fault-detail by file resource attribute and inline text value at the same time! " +
                            "Please choose one of them.");
                }
                
                String filePath = faultDetailElement.getAttribute("file");
                
                beanDefinition.addPropertyValue("faultDetailResource", FileUtils.getResourceFromFilePath(filePath));
            } else {
                String faultDetailData = DomUtils.getTextValue(faultDetailElement).trim();
                if (StringUtils.hasText(faultDetailData)) {
                    beanDefinition.addPropertyValue("faultDetail", faultDetailData);
                } else {
                    throw new BeanCreationException("Not content for fault-detail is set! Either use file attribute or inline text value for fault-detail element.");
                }
            }
        }
        
        Map<String, BeanDefinitionParser> actionRegistry = TestActionRegistry.getRegisteredActionParser();
        Element action;
        if (faultDetailElement == null) {
            action = DOMUtil.getFirstChildElement(element);
        } else {
            action = DOMUtil.getNextSiblingElement(faultDetailElement);
        }
        
        if (action != null && action.getTagName().equals("description")) {
            action = DOMUtil.getNextSiblingElement(action);
        }

        if (action != null) {
            BeanDefinitionParser parser = actionRegistry.get(action.getTagName());
            
            if (parser ==  null) {
            	beanDefinition.addPropertyValue("action", parserContext.getReaderContext().getNamespaceHandlerResolver().resolve(action.getNamespaceURI()).parse(action, parserContext));
            } else {
            	beanDefinition.addPropertyValue("action", parser.parse(action, parserContext));
            }
        }

        BeanDefinitionParserUtils.setPropertyReference(beanDefinition, element.getAttribute("fault-validator"), "validator", "soapFaultValidator");
        BeanDefinitionParserUtils.setPropertyReference(beanDefinition, element.getAttribute("message-factory"), "messageFactory", "messageFactory");
        
        return beanDefinition.getBeanDefinition();
    }
}
