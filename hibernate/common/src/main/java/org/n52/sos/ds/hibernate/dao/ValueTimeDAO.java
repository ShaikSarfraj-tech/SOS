/*
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
package org.n52.sos.ds.hibernate.dao;


import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.n52.shetland.ogc.gml.time.IndeterminateValue;
import org.n52.shetland.ogc.ows.exception.CodedException;
import org.n52.shetland.ogc.ows.exception.OwsExceptionReport;
import org.n52.shetland.ogc.sos.ExtendedIndeterminateTime;
import org.n52.shetland.ogc.sos.request.GetObservationRequest;
import org.n52.shetland.util.CollectionHelper;
import org.n52.sos.ds.hibernate.dao.observation.AbstractValueDAO;
import org.n52.sos.ds.hibernate.entities.FeatureOfInterest;
import org.n52.sos.ds.hibernate.entities.ObservableProperty;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.n52.sos.ds.hibernate.entities.Procedure;
import org.n52.sos.ds.hibernate.entities.observation.legacy.TemporalReferencedLegacyObservation;
import org.n52.sos.ds.hibernate.util.HibernateHelper;

/**
 * Implementation of {@link AbstractValueDAO} for old concept to query only time information
 * @author <a href="mailto:c.hollmann@52north.org">Carsten Hollmann</a>
 * @since 4.1.0
 *
 */
public class ValueTimeDAO extends AbstractValueDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValueTimeDAO.class);

    /**
     * Query the minimum {@link TemporalReferencedLegacyObservation} for parameter
     * @param request
     *            {@link GetObservationRequest}
     * @param procedure
     *            Datasource procedure id
     * @param observableProperty
     *            Datasource procedure id
     * @param featureOfInterest
     *            Datasource procedure id
     * @param temporalFilterCriterion
     *            Temporal filter {@link Criterion}
     * @param session
     *            Hibernate Session
     * @return Resulting minimum {@link TemporalReferencedLegacyObservation}
     * @throws OwsExceptionReport If an error occurs when executing the query
     */
    public TemporalReferencedLegacyObservation getMinValueFor(GetObservationRequest request, long procedure, long observableProperty,
            long featureOfInterest, Criterion temporalFilterCriterion, Session session) throws OwsExceptionReport {
        return (TemporalReferencedLegacyObservation) getValueCriteriaFor(request, procedure, observableProperty, featureOfInterest,
                temporalFilterCriterion, ExtendedIndeterminateTime.FIRST, session).uniqueResult();
    }

    /**
     * Query the maximum {@link TemporalReferencedLegacyObservation} for parameter
     * @param request
     *            {@link GetObservationRequest}
     * @param procedure
     *            Datasource procedure id
     * @param observableProperty
     *            Datasource procedure id
     * @param featureOfInterest
     *            Datasource procedure id
     * @param temporalFilterCriterion
     *            Temporal filter {@link Criterion}
     * @param session
     *            Hibernate Session
     * @return Resulting maximum {@link TemporalReferencedLegacyObservation}
     * @throws OwsExceptionReport If an error occurs when executing the query
     */
    public TemporalReferencedLegacyObservation getMaxValueFor(GetObservationRequest request, long procedure, long observableProperty,
            long featureOfInterest, Criterion temporalFilterCriterion, Session session) throws OwsExceptionReport {
        return (TemporalReferencedLegacyObservation) getValueCriteriaFor(request, procedure, observableProperty, featureOfInterest,
                temporalFilterCriterion, ExtendedIndeterminateTime.LATEST, session).uniqueResult();
    }

    /**
     * Query the minimum {@link TemporalReferencedLegacyObservation} for parameter
     * @param request
     *            {@link GetObservationRequest}
     * @param procedure
     *            Datasource procedure id
     * @param observableProperty
     *            Datasource procedure id
     * @param featureOfInterest
     *            Datasource procedure id
     * @param session
     *            Hibernate Session
     * @return Resulting minimum {@link TemporalReferencedLegacyObservation}
     * @throws OwsExceptionReport If an error occurs when executing the query
     */
    public TemporalReferencedLegacyObservation getMinValueFor(GetObservationRequest request, long procedure, long observableProperty,
            long featureOfInterest, Session session) throws OwsExceptionReport {
        return (TemporalReferencedLegacyObservation) getValueCriteriaFor(request, procedure, observableProperty, featureOfInterest, null,
                ExtendedIndeterminateTime.FIRST, session).uniqueResult();
    }

    /**
     * Query the maximum {@link TemporalReferencedLegacyObservation} for parameter
     * @param request
     *            {@link GetObservationRequest}
     * @param procedure
     *            Datasource procedure id
     * @param observableProperty
     *            Datasource procedure id
     * @param featureOfInterest
     *            Datasource procedure id
     * @param session
     *            Hibernate Session
     * @return Resulting maximum {@link TemporalReferencedLegacyObservation}
     * @throws OwsExceptionReport If an error occurs when executing the query
     */
    public TemporalReferencedLegacyObservation getMaxValueFor(GetObservationRequest request, long procedure, long observableProperty,
            long featureOfInterest, Session session) throws OwsExceptionReport {
        return (TemporalReferencedLegacyObservation) getValueCriteriaFor(request, procedure, observableProperty, featureOfInterest, null,
                ExtendedIndeterminateTime.LATEST, session).uniqueResult();
    }

    /**
     * Create {@link Criteria} for parameter
     * @param request
     *            {@link GetObservationRequest}
     * @param procedure
     *            Datasource procedure id
     * @param observableProperty
     *            Datasource procedure id
     * @param featureOfInterest
     *            Datasource procedure id
     * @param temporalFilterCriterion
     *            Temporal filter {@link Criterion}
     * @param sosIndeterminateTime first/latest indicator
     * @param session
     *            Hibernate Session
     * @return Resulting {@link Criteria}
     * @throws OwsExceptionReport  If an error occurs when adding Spatial Filtering Profile
     *             restrictions
     */
    private Criteria getValueCriteriaFor(GetObservationRequest request, long procedure, long observableProperty,
            long featureOfInterest, Criterion temporalFilterCriterion, IndeterminateValue sosIndeterminateTime,
            Session session) throws OwsExceptionReport {
        final Criteria c =
                getDefaultObservationCriteria(TemporalReferencedLegacyObservation.class, session)
                        .createAlias(TemporalReferencedLegacyObservation.PROCEDURE, "p")
                        .createAlias(TemporalReferencedLegacyObservation.FEATURE_OF_INTEREST, "f")
                        .createAlias(TemporalReferencedLegacyObservation.OBSERVABLE_PROPERTY, "o");

        checkAndAddSpatialFilteringProfileCriterion(c, request, session);

        c.add(Restrictions.eq("p." + Procedure.ID, observableProperty));
        c.add(Restrictions.eq("o." + ObservableProperty.ID, observableProperty));
        c.add(Restrictions.eq("f." + FeatureOfInterest.ID, featureOfInterest));

        if (CollectionHelper.isNotEmpty(request.getOfferings())) {
            c.createCriteria(TemporalReferencedLegacyObservation.OFFERINGS).add(Restrictions.in(Offering.IDENTIFIER, request.getOfferings()));
        }

        String logArgs = "request, series, offerings";
        if (temporalFilterCriterion != null) {
            logArgs += ", filterCriterion";
            c.add(temporalFilterCriterion);
        }
        if (sosIndeterminateTime != null) {
            logArgs += ", sosIndeterminateTime";
            addIndeterminateTimeRestriction(c, sosIndeterminateTime);
        }
        addSpecificRestrictions(c, request);
        LOGGER.debug("QUERY getObservationFor({}): {}", logArgs, HibernateHelper.getSqlString(c));
        return c;
    }

    /**
     * Get default {@link Criteria} for {@link Class}
     *
     * @param clazz
     *            {@link Class} to get default {@link Criteria} for
     * @param session
     *            Hibernate Session
     * @return Default {@link Criteria}
     */
    public Criteria getDefaultObservationCriteria(Class<?> clazz, Session session) {
        return session.createCriteria(clazz).add(Restrictions.eq(TemporalReferencedLegacyObservation.DELETED, false))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    }

    @Override
    protected void addSpecificRestrictions(Criteria c, GetObservationRequest request) throws CodedException {
        // nothing  to add
    }
}
