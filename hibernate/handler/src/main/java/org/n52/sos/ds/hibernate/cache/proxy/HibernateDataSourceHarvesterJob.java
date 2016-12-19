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
package org.n52.sos.ds.hibernate.cache.proxy;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import javax.inject.Inject;

import org.hibernate.Session;
import org.n52.iceland.ds.ConnectionProvider;
import org.n52.iceland.event.ServiceEventBus;
import org.n52.io.task.ScheduledJob;
import org.n52.proxy.db.beans.RelatedFeatureEntity;
import org.n52.proxy.db.da.InsertRepository;
import org.n52.series.db.beans.DatasetEntity;
import org.n52.series.db.beans.OfferingEntity;
import org.n52.series.db.beans.ServiceEntity;
import org.n52.shetland.ogc.ows.exception.CodedException;
import org.n52.sos.ds.hibernate.HibernateSessionHolder;
import org.n52.sos.ds.hibernate.dao.DaoFactory;
import org.n52.sos.ds.hibernate.dao.OfferingDAO;
import org.n52.sos.ds.hibernate.dao.observation.series.AbstractSeriesDAO;
import org.n52.sos.ds.hibernate.entities.Offering;
import org.n52.sos.ds.hibernate.entities.RelatedFeature;
import org.n52.sos.ds.hibernate.entities.TOffering;
import org.n52.sos.ds.hibernate.entities.observation.series.Series;
import org.n52.sos.ds.hibernate.util.HibernateHelper;
import org.n52.sos.event.events.UpdateCache;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class HibernateDataSourceHarvesterJob extends ScheduledJob implements Job {

    private final static Logger LOGGER = LoggerFactory.getLogger(HibernateDataSourceHarvesterJob.class);

    @Inject
    private InsertRepository insertRepository;
    private HibernateSessionHolder sessionHolder;
    private ServiceEventBus serviceEventBus;

    @Inject
    public void setConnectionProvider(ConnectionProvider connectionProvider) {
        this.sessionHolder = new HibernateSessionHolder(connectionProvider);
    }

    public HibernateSessionHolder getConnectionProvider() {
        return this.sessionHolder;
    }

    public InsertRepository getInsertRepository() {
        return insertRepository;
    }

    public void setInsertRepository(InsertRepository insertRepository) {
        this.insertRepository = insertRepository;
    }

    @Inject
    public void setServiceEventBus(ServiceEventBus serviceEventBus) {
        this.serviceEventBus = serviceEventBus;
    }

    public ServiceEventBus getServiceEventBus() {
        return serviceEventBus;
    }

    @Override
    public JobDetail createJobDetails() {
        return JobBuilder.newJob(HibernateDataSourceHarvesterJob.class)
                .withIdentity(getJobName())
                .build();
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Session session = null;
        try {
            LOGGER.info(context.getJobDetail().getKey() + " execution starts.");
            session = getConnectionProvider().getSession();
            ServiceEntity service = insertRepository.insertService(EntityBuilder.createService("localDB", "description of localDB", "localhost", "2.0.0"));
            insertRepository.cleanUp(service);
            insertRepository.prepareInserting(service);
            harvestOfferings(service, session);
            harvestSeries(service, session);
            harvestRelatedFeartures(service, session);
            LOGGER.info(context.getJobDetail().getKey() + " execution ends.");
            getServiceEventBus().submit(new UpdateCache());
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(HibernateDataSourceHarvesterJob.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            getConnectionProvider().returnSession(session);
        }
    }

    private void harvestOfferings(ServiceEntity service, Session session) {
        for (Offering offering : new OfferingDAO().getOfferings(session)) {
            OfferingEntity offferingEntity = EntityBuilder.createOffering(offering, service, true, true);
            // TODO add phenTime, ResultTime, ...

            insertRepository.insertOffering(offferingEntity);
        }
    }

    private void harvestSeries(ServiceEntity service, Session session) throws CodedException {
        AbstractSeriesDAO seriesDAO = DaoFactory.getInstance().getSeriesDAO();
        for (Series series : seriesDAO.getSeries(session)) {
            DatasetEntity<?> dataset = EntityBuilder.createDataset(series, service);
            if (dataset != null) {
                insertRepository.insertDataset(dataset);
            }
        }
    }

    private void harvestRelatedFeartures(ServiceEntity service, Session session) {
        if (HibernateHelper.isEntitySupported(TOffering.class)) {
            Set<RelatedFeatureEntity> relatedFeatures = new HashSet<>();
            for (Offering offering : new OfferingDAO().getOfferings(session)) {
                if (offering instanceof TOffering && ((TOffering) offering).hasRelatedFeatures()) {
                    for (RelatedFeature relatedFeatureEntity : ((TOffering) offering).getRelatedFeatures()) {
                        relatedFeatures.add(EntityBuilder.createRelatedFeature(relatedFeatureEntity, service));
                    }
                }
            }
            insertRepository.insertRelatedFeature(relatedFeatures);
        }
    }

}
