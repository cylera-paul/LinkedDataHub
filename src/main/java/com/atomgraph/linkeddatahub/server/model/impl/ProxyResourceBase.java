/**
 *  Copyright 2019 Martynas Jusevičius <martynas@atomgraph.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.atomgraph.linkeddatahub.server.model.impl;

import com.atomgraph.client.MediaTypes;
import com.atomgraph.client.util.DataManager;
import com.atomgraph.client.vocabulary.AC;
import com.atomgraph.linkeddatahub.client.filter.WebIDDelegationFilter;
import com.atomgraph.linkeddatahub.model.Agent;
import com.atomgraph.linkeddatahub.server.model.ClientUriInfo;
import java.net.URI;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Linked Data proxy resource.
 * Forwards Linked Data request to a remote location.
 * The location is identified indirectly using a URL parameter.
 * 
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class ProxyResourceBase extends com.atomgraph.client.model.impl.ProxyResourceBase
{

    private static final Logger log = LoggerFactory.getLogger(ProxyResourceBase.class);

    private final DataManager dataManager;
    
    @Inject
    public ProxyResourceBase(@Context UriInfo uriInfo, ClientUriInfo clientUriInfo, @Context Request request, @Context HttpHeaders httpHeaders, MediaTypes mediaTypes, @Context SecurityContext securityContext,
            @QueryParam("uri") URI uri, @QueryParam("endpoint") URI endpoint, @QueryParam("accept") MediaType accept, @QueryParam("mode") URI mode,
            com.atomgraph.linkeddatahub.Application system, @Context HttpServletRequest httpServletRequest,
            DataManager dataManager)
    {
        super(clientUriInfo, request, httpHeaders, mediaTypes,
                clientUriInfo.getQueryParameters().getFirst(AC.uri.getLocalName()) == null ? null : URI.create(clientUriInfo.getQueryParameters().getFirst(AC.uri.getLocalName())),
                clientUriInfo.getQueryParameters().getFirst(AC.endpoint.getLocalName()) == null ? null : URI.create(clientUriInfo.getQueryParameters().getFirst(AC.endpoint.getLocalName())),
                clientUriInfo.getQueryParameters().getFirst(AC.accept.getLocalName()) == null ? null : MediaType.valueOf(clientUriInfo.getQueryParameters().getFirst(AC.accept.getLocalName())),
                mode, system.getClient(), httpServletRequest);
        this.dataManager = dataManager;
        
        if (securityContext.getUserPrincipal() instanceof Agent &&
            securityContext.getAuthenticationScheme().equals(SecurityContext.CLIENT_CERT_AUTH))
            super.getWebTarget().register(new WebIDDelegationFilter((Agent)securityContext.getUserPrincipal()));
    }
    
    public DataManager getDataManager()
    {
        return dataManager;
    }
    
}
