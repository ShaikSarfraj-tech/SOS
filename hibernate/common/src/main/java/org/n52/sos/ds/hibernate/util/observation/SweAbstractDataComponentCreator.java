/*
 * Copyright (C) 2012-2019 52°North Initiative for Geospatial Open Source
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
package org.n52.sos.ds.hibernate.util.observation;

import java.net.URI;
import java.net.URISyntaxException;

import org.n52.janmayen.NcName;
import org.n52.series.db.beans.BlobDataEntity;
import org.n52.series.db.beans.BooleanDataEntity;
import org.n52.series.db.beans.CategoryDataEntity;
import org.n52.series.db.beans.ComplexDataEntity;
import org.n52.series.db.beans.CountDataEntity;
import org.n52.series.db.beans.DataArrayDataEntity;
import org.n52.series.db.beans.DataEntity;
import org.n52.series.db.beans.GeometryDataEntity;
import org.n52.series.db.beans.PhenomenonEntity;
import org.n52.series.db.beans.ProfileDataEntity;
import org.n52.series.db.beans.QuantityDataEntity;
import org.n52.series.db.beans.ReferencedDataEntity;
import org.n52.series.db.beans.TextDataEntity;
import org.n52.shetland.ogc.gml.CodeType;
import org.n52.shetland.ogc.ows.exception.CodedException;
import org.n52.shetland.ogc.ows.exception.NoApplicableCodeException;
import org.n52.shetland.ogc.ows.exception.OwsExceptionReport;
import org.n52.shetland.ogc.swe.SweAbstractDataComponent;
import org.n52.shetland.ogc.swe.SweDataArray;
import org.n52.shetland.ogc.swe.SweDataRecord;
import org.n52.shetland.ogc.swe.SweField;
import org.n52.shetland.ogc.swe.simpleType.SweAbstractUomType;
import org.n52.shetland.ogc.swe.simpleType.SweBoolean;
import org.n52.shetland.ogc.swe.simpleType.SweCategory;
import org.n52.shetland.ogc.swe.simpleType.SweCount;
import org.n52.shetland.ogc.swe.simpleType.SweQuantity;
import org.n52.shetland.ogc.swe.simpleType.SweText;
import org.n52.svalbard.decode.DecoderRepository;

/**
 * {@code ValuedObservationVisitor} to create {@link SweAbstractDataEntityComponent}
 * from observations.
 *
 * @author Christian Autermann
 */
public class SweAbstractDataComponentCreator
        implements ValuedObservationVisitor<SweAbstractDataComponent> {

    private DecoderRepository decoderRepository;

    public SweAbstractDataComponentCreator(DecoderRepository decoderRepository) {
        this.decoderRepository = decoderRepository;
    }

    public SweAbstractDataComponent visit(DataEntity sub) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SweAbstractDataComponent visit(GeometryDataEntity o)
            throws OwsExceptionReport {
        // TODO implement SweEnvelope/SweCoordinte etc.
        throw notSupported(o);
    }

    @Override
    public SweAbstractDataComponent visit(BlobDataEntity o)
            throws OwsExceptionReport {
        throw notSupported(o);
    }

    @Override
    public SweQuantity visit(QuantityDataEntity o) throws CodedException {
        SweQuantity component = new SweQuantity();
        component.setValue(o.getValue());
        return setCommonValues(component, o);
    }

    @Override
    public SweBoolean visit(BooleanDataEntity o) throws CodedException {
        SweBoolean component = new SweBoolean();
        component.setValue(o.getValue());
        return setCommonValues(component, o);
    }

    @Override
    public SweCategory visit(CategoryDataEntity o) throws CodedException {
        SweCategory component = new SweCategory();
        component.setValue(o.getValue());
        return setCommonValues(component, o);
    }

    @Override
    public SweDataRecord visit(ComplexDataEntity o)
            throws OwsExceptionReport {
        SweDataRecord record = new SweDataRecord();
        for (DataEntity<?> sub : o.getValue()) {
            String fieldName = getFieldName(sub);
            record.addField(new SweField(fieldName, this.visit(sub)));
        }
        return setCommonValues(record, o);
    }

    @Override
    public SweCount visit(CountDataEntity o) throws CodedException {
        SweCount component = new SweCount();
        component.setValue(o.getValue());
        return setCommonValues(component, o);
    }

    @Override
    public SweText visit(TextDataEntity o)
            throws OwsExceptionReport {
        SweText component = new SweText();
        component.setValue(o.getValue());
        return setCommonValues(component, o);
    }

    @Override
    public SweDataArray visit(DataArrayDataEntity o) throws OwsExceptionReport {
        SweDataArray array = new SweDataArray();
        // TODO
        return array;
    }

    @Override
    public SweAbstractDataComponent visit(ProfileDataEntity o) throws OwsExceptionReport {
        return new ProfileGeneratorSplitter(new ObservationValueCreator(decoderRepository)).createValue(o);
    }

    @Override
    public SweCategory visit(ReferencedDataEntity o) throws OwsExceptionReport {
        SweCategory component = new SweCategory();
        if (o.hasValue()) {
            component.setValue(o.getValue());
        } else if (o.hasValueName()) {
            component.setValue(o.getValueName());
        }
        return setCommonValues(component, o);
    }

    protected String getFieldName(DataEntity<?> sub) {
        String name = sub.getDataset().getObservableProperty().getName();
        if (name != null && !name.isEmpty()) {
            return name;
        }
        return NcName.makeValid(sub.getDataset().getObservableProperty().getIdentifier());
    }

    protected <T extends SweAbstractDataComponent> T setCommonValues(
            T component, DataEntity<?> valuedObservation) throws CodedException {
        if (valuedObservation != null) {
            PhenomenonEntity op = valuedObservation.getDataset().getPhenomenon();
            component.setIdentifier(op.getIdentifier());
            component.setDefinition(op.getIdentifier());
            component.setDescription(op.getDescription());
            if (op.isSetNameCodespace()) {
                String codespace = op.getNameCodespace().getName();
                try {
                    component.setName(new CodeType(op.getName(), new URI(codespace)));
                } catch (URISyntaxException e) {
                    throw new NoApplicableCodeException().causedBy(e).withMessage(
                            "Error while creating URI from '{}'", codespace);
                }
            } else {
                component.setName(op.getName());
            }

            if (valuedObservation.getDataset().hasUnit() &&
                component instanceof SweAbstractUomType) {
                SweAbstractUomType<?> uomType = (SweAbstractUomType<?>) component;
                uomType.setUom(valuedObservation.getDataset().getUnit().getUnit());
            }
        }
        return component;
    }

    protected OwsExceptionReport notSupported(DataEntity<?> o) {
        return new NoApplicableCodeException()
                .withMessage("Complex observation fields of type %s" +
                             " are currently not supported", o.getValue());
    }

}
