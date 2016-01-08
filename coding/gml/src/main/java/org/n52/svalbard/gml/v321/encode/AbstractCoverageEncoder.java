/**
 * Copyright (C) 2012-2016 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.svalbard.gml.v321.encode;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlObject;
import org.n52.sos.encode.AbstractSpecificXmlEncoder;
import org.n52.sos.ogc.gml.GmlConstants;
import org.n52.sos.ogc.om.values.BooleanValue;
import org.n52.sos.ogc.om.values.CategoryValue;
import org.n52.sos.ogc.om.values.CountValue;
import org.n52.sos.ogc.om.values.DiscreteCoverage;
import org.n52.sos.ogc.om.values.QuantityValue;
import org.n52.sos.ogc.om.values.Value;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.util.XmlHelper;

import com.google.common.collect.Lists;

import net.opengis.gml.x32.BooleanListDocument;
import net.opengis.gml.x32.CategoryListDocument;
import net.opengis.gml.x32.CodeOrNilReasonListType;
import net.opengis.gml.x32.CountListDocument;
import net.opengis.gml.x32.DiscreteCoverageType;
import net.opengis.gml.x32.MeasureOrNilReasonListType;
import net.opengis.gml.x32.QuantityListDocument;
import net.opengis.gml.x32.RangeSetType;

public abstract class AbstractCoverageEncoder<T, S> extends AbstractSpecificXmlEncoder<T, S> {
    
    protected RangeSetType encodeRangeSet(DiscreteCoverageType dct, DiscreteCoverage<?> discreteCoverage) throws OwsExceptionReport {
        RangeSetType rst = dct.addNewRangeSet();
        encodeValueList(rst, discreteCoverage);
        return dct.getRangeSet();
    }

    protected void encodeValueList(RangeSetType rst, DiscreteCoverage<?> discreteCoverage) throws OwsExceptionReport {
        List list = getList(discreteCoverage);
        Value<?> value = discreteCoverage.getRangeSet().iterator().next();
        if (value instanceof BooleanValue) {
            BooleanListDocument bld = BooleanListDocument.Factory.newInstance();
            bld.setBooleanList(list);
            rst.set(bld);
        } else if (value instanceof CategoryValue) {
            CategoryListDocument cld  = CategoryListDocument.Factory.newInstance();
            CodeOrNilReasonListType conrlt = cld.addNewCategoryList();
            conrlt.setCodeSpace(discreteCoverage.getUnit());
            conrlt.setListValue(list);
            rst.set(cld);
        } else if (value instanceof CountValue) {
            CountListDocument cld = CountListDocument.Factory.newInstance();
            cld.setCountList(list);
            rst.set(cld);
        } else if (value instanceof QuantityValue) {
            QuantityListDocument qld = QuantityListDocument.Factory.newInstance();
            MeasureOrNilReasonListType monrlt = qld.addNewQuantityList();
            monrlt.setUom(discreteCoverage.getUnit());
            monrlt.setListValue(list);
            rst.set(qld);
        } else {
            rst.setNil();
        }
    }


    private List getList(DiscreteCoverage<?> discreteCoverage) {
        List list = Lists.newArrayList();
        for (Object value : discreteCoverage.getRangeSet()) {
            if (value instanceof Value<?>) {
                list.add(((Value<?>) value).getValue());
            }
        }
        return list;
    }
    
}
