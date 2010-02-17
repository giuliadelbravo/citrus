/*
 * Copyright 2006-2010 ConSol* Software GmbH.
 * 
 * This file is part of Citrus.
 * 
 * Citrus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citrus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Citrus. If not, see <http://www.gnu.org/licenses/>.
 */

package com.consol.citrus.config.xml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Abstract parser implementation for all iterative container actions. Parser takes care of
 * index name, aborting condition, index start value and description
 * 
 * @author Christoph Deppisch
 */
public abstract class AbstractIterationTestActionParser  implements BeanDefinitionParser {

    /**
     * @see org.springframework.beans.factory.xml.BeanDefinitionParser#parse(org.w3c.dom.Element, org.springframework.beans.factory.xml.ParserContext)
     */
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder builder = parseComponent(element, parserContext);
        
        DescriptionElementParser.doParse(element, builder);

        String index = element.getAttribute("index");
        if (StringUtils.hasText(index)) {
            builder.addPropertyValue("indexName", index);
        }

        String condition = element.getAttribute("condition");
        builder.addPropertyValue("condition", condition);

        builder.addPropertyValue("name", element.getLocalName());
        
        ActionContainerParser.doParse(element, parserContext, builder);
        
        return builder.getBeanDefinition();
    }
    
    protected abstract BeanDefinitionBuilder parseComponent(Element element, ParserContext parserContext);
}