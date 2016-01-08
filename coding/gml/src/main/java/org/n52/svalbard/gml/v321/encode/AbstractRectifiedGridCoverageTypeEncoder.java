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

import java.util.Map;

import org.n52.sos.ogc.gml.GmlConstants;
import org.n52.sos.ogc.om.values.RectifiedGridCoverage;
import org.n52.sos.ogc.ows.OwsExceptionReport;
import org.n52.sos.ogc.sos.SosConstants.HelperValues;

import net.opengis.gml.x32.DiscreteCoverageType;
import net.opengis.gml.x32.DomainSetType;
import net.opengis.gml.x33.ce.SimpleMultiPointDocument;
import net.opengis.gml.x33.ce.SimpleMultiPointType;


public abstract class AbstractRectifiedGridCoverageTypeEncoder<T> extends AbstractCoverageEncoder<T, RectifiedGridCoverage> {

    protected DiscreteCoverageType encodeRectifiedGridCoverage(RectifiedGridCoverage rectifiedGridCoverage, Map<HelperValues, String> additionalValues) throws OwsExceptionReport {
        DiscreteCoverageType dct = DiscreteCoverageType.Factory.newInstance();
        dct.setId(rectifiedGridCoverage.getGmlId());
        dct.setDomainSet(encodeDomainSet(rectifiedGridCoverage));
        dct.setRangeSet(encodeRangeSet(dct, rectifiedGridCoverage));
        return dct;
    }
    
    private DomainSetType encodeDomainSet(RectifiedGridCoverage rectifiedGridCoverage) {
        DomainSetType dst = DomainSetType.Factory.newInstance();
        SimpleMultiPointDocument smpd = SimpleMultiPointDocument.Factory.newInstance();
        SimpleMultiPointType smpt = smpd.addNewSimpleMultiPoint();
        smpt.addNewPosList().setListValue(rectifiedGridCoverage.getDomainSet());
        dst.set(smpt);
        return dst;
    }

    @Override
    public void addNamespacePrefixToMap(Map<String, String> nameSpacePrefixMap) {
        super.addNamespacePrefixToMap(nameSpacePrefixMap);
        nameSpacePrefixMap.put(GmlConstants.NS_GML_32, GmlConstants.NS_GML_PREFIX);
    }

}
