/*******************************************************************************
 * Copyright (c) 2010, 2011 Ed Anuff and Usergrid, all rights reserved.
 * http://www.usergrid.com
 * 
 * This file is part of Usergrid Stack.
 * 
 * Usergrid Stack is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 * 
 * Usergrid Stack is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License along
 * with Usergrid Stack. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU AGPL version 3 section 7
 * 
 * Linking Usergrid Stack statically or dynamically with other modules is making
 * a combined work based on Usergrid Stack. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 * 
 * In addition, as a special exception, the copyright holders of Usergrid Stack
 * give you permission to combine Usergrid Stack with free software programs or
 * libraries that are released under the GNU LGPL and with independent modules
 * that communicate with Usergrid Stack solely through:
 * 
 *   - Classes implementing the org.usergrid.services.Service interface
 *   - Apache Shiro Realms and Filters
 *   - Servlet Filters and JAX-RS/Jersey Filters
 * 
 * You may copy and distribute such a system following the terms of the GNU AGPL
 * for Usergrid Stack and the licenses of the other code concerned, provided that
 ******************************************************************************/
package org.usergrid.rest.queues;

import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.usergrid.mq.QueueManager;
import org.usergrid.mq.QueueSet;
import org.usergrid.rest.AbstractContextResource;

import com.sun.jersey.api.json.JSONWithPadding;

@Produces({ MediaType.APPLICATION_JSON, "application/javascript",
		"application/x-javascript", "text/ecmascript",
		"application/ecmascript", "text/jscript" })
public class QueueSubscriptionResource extends AbstractContextResource {

	static final Logger logger = LoggerFactory
			.getLogger(QueueSubscriptionResource.class);

	QueueManager mq;
	String queuePath = "";
	String subscriptionPath = "";

	public QueueSubscriptionResource(QueueResource parent, QueueManager mq,
			String queuePath) throws Exception {
		super(parent);

		this.mq = mq;
		this.queuePath = queuePath;

	}

	public QueueSubscriptionResource(QueueSubscriptionResource parent,
			QueueManager mq, String queuePath, String subscriptionPath)
			throws Exception {
		super(parent);

		this.mq = mq;
		this.queuePath = queuePath;
		this.subscriptionPath = subscriptionPath;
	}

	@Path("{subPath}")
	public QueueSubscriptionResource getSubPath(@Context UriInfo ui,
			@PathParam("subPath") String subPath) throws Exception {

		logger.info("QueueSubscriptionResource.getSubPath");

		return new QueueSubscriptionResource(this, mq, queuePath,
				subscriptionPath + "/" + subPath);
	}

	@GET
	public JSONWithPadding executeGet(@Context UriInfo ui,
			@QueryParam("start") String firstSubscriptionQueuePath,
			@QueryParam("limit") @DefaultValue("10") int limit,
			@QueryParam("callback") @DefaultValue("callback") String callback)
			throws Exception {

		logger.info("QueueSubscriptionResource.executeGet: " + queuePath);

		QueueSet results = mq.getSubscriptions(queuePath,
				firstSubscriptionQueuePath, limit);

		return new JSONWithPadding(results, callback);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public JSONWithPadding executePost(@Context UriInfo ui,
			Map<String, Object> json,
			@QueryParam("callback") @DefaultValue("callback") String callback)
			throws Exception {

		logger.info("QueueSubscriptionResource.executePost: " + queuePath);

		return executePut(ui, json, callback);
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public JSONWithPadding executePut(@Context UriInfo ui,
			Map<String, Object> json,
			@QueryParam("callback") @DefaultValue("callback") String callback)
			throws Exception {

		logger.info("QueueSubscriptionResource.executePut: " + queuePath);

		if (StringUtils.isNotBlank(subscriptionPath)) {
			return new JSONWithPadding(mq.subscribeToQueue(subscriptionPath,
					queuePath), callback);
		} else if ((json != null) && (json.containsKey("subscriber"))) {
			String supscription = (String) json.get("supscription");
			return new JSONWithPadding(mq.subscribeToQueue(supscription,
					queuePath), callback);
		} else if ((json != null) && (json.containsKey("subscribers"))) {
			@SuppressWarnings("unchecked")
			List<String> supscriptions = (List<String>) json
					.get("supscriptions");
			return new JSONWithPadding(mq.unsubscribeFromQueues(queuePath,
					supscriptions), callback);
		}

		return null;
	}

	@DELETE
	public JSONWithPadding executeDelete(@Context UriInfo ui,
			@QueryParam("callback") @DefaultValue("callback") String callback)
			throws Exception {

		logger.info("QueueSubscriptionResource.executeDelete: " + queuePath);

		if (StringUtils.isNotBlank(subscriptionPath)) {
			return new JSONWithPadding(mq.unsubscribeFromQueue(
					subscriptionPath, queuePath), callback);
		}

		return null;
	}

}
