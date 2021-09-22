/*-
 * #%L
 * Extended authenticators for Cloud Endpoints v2
 * ---
 * Copyright (C) 2018 AODocs (Altirnao Inc)
 * ---
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.aodocs.endpoints.context;

import com.aodocs.endpoints.util.AsyncRefreshMemoizingSupplier;
import com.aodocs.endpoints.util.cache.ObjectCache;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.services.iam.v1.Iam;
import com.google.api.services.iam.v1.IamScopes;
import com.google.api.services.iam.v1.model.ListServiceAccountsResponse;
import com.google.api.services.iam.v1.model.ServiceAccount;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.Identity;
import com.google.cloud.Role;
import com.google.cloud.resourcemanager.Project;
import com.google.cloud.resourcemanager.ResourceManager;
import com.google.cloud.resourcemanager.ResourceManagerOptions;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.java.Log;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fetches project roles and update them regularly
 */
@Log
public class ProjectConfigProvider {

    //TODO make this configurable
    private static final int TTL_IN_SECONDS = 60 * 10;

    private static final Pattern CLIENT_ID_PATTERN = Pattern.compile("(\\d+)(-\\w+)?.apps.googleusercontent.com");

    public static String extractProjectNumber(String clientId) {
        Matcher matcher = CLIENT_ID_PATTERN.matcher(clientId);
        if (!matcher.matches())
            return null;
        return matcher.group(1);
    }

    @Value
    public class ProjectConfig {
        /**
         * Represents all roles
         */
        ImmutableMap<Role, ImmutableSet<Identity>> roleBindings;
        /**
         * key: service account's email, value: service account's oauth2 client Id.
         * Old format example: 428563709008-vvh1k92tpns1ab8qnhum5fetmk4iir47.apps.googleusercontent.com
         * New format example: 115933665684941698610
         */
        ImmutableBiMap<String, String> serviceAccountClientIds;

        Instant refreshedAt = Instant.now();

	    /**
	     * Returns the roles for the provided email address.
	     * Does not resolve user groups!
	     *
	     * @param email an email address
	     * @return a list of roles matching this address, with "roles/" prefix removed
	     */
	    public ImmutableSet<String> getRolesFor(@NonNull String email) {
		    ImmutableSet.Builder<String> roles = ImmutableSet.builder();
		    for (Map.Entry<Role, ImmutableSet<Identity>> entry : roleBindings.entrySet()) {
			    for (Identity identity : entry.getValue()) {
				    if (matchesEmail(email, identity) || matchesUserDomain(email, identity) || matchesAnyUser(identity)) {
				    	roles.add(entry.getKey().getValue().replaceFirst("^roles/", ""));
				    }
			    }
		    }
		    return roles.build();
	    }

	    boolean matchesEmail(String email, Identity identity) {
		    return ImmutableSet.of(Identity.Type.USER, Identity.Type.SERVICE_ACCOUNT, Identity.Type.GROUP)
				    .contains(identity.getType()) && identity.getValue().equals(email);
	    }

	    boolean matchesUserDomain(String email, Identity identity) {
		    return identity.getType().equals(Identity.Type.DOMAIN) && email.endsWith("@" + identity.getValue());
	    }

	    boolean matchesAnyUser(Identity identity) {
		    return ImmutableSet.of(Identity.Type.ALL_USERS, Identity.Type.ALL_AUTHENTICATED_USERS)
				    .contains(identity.getType());
	    }
    }

	private final ResourceManager resourceManager;
	private final Iam iamClient;
	private final String applicationId;
	private final Project project;

    //used as a singleton cache for auto refresh, key is only used for the memcache
    private final AsyncRefreshMemoizingSupplier<ProjectConfig> mutableConfigCache =
            AsyncRefreshMemoizingSupplier.create(TTL_IN_SECONDS, this::getProjectConfig);

    private static ProjectConfigProvider INSTANCE;

    public static ProjectConfigProvider get() {
    	if (INSTANCE == null) {
			INSTANCE = new ProjectConfigProvider();
		}
		return INSTANCE;
	}

	@VisibleForTesting //TODO use proper injection instead
	public static void override(String applicationId, ResourceManager resourceManager, Iam iamClient) {
		INSTANCE = new ProjectConfigProvider(applicationId, resourceManager, iamClient);
	}

    private ProjectConfigProvider() {
        this(AppengineHelper.getApplicationId(),
		        ResourceManagerOptions.getDefaultInstance().getService(),
		        defaultIamClient(AppengineHelper.getApplicationId()));
    }

    @SneakyThrows
	static Iam defaultIamClient(String applicationName) {
		GoogleCredentials credentials = GoogleCredentials.getApplicationDefault().createScoped(Collections.singleton(IamScopes.CLOUD_PLATFORM));
		return new Iam.Builder(Utils.getDefaultTransport(), Utils.getDefaultJsonFactory(),
				new HttpCredentialsAdapter(credentials))
		.setApplicationName(applicationName).build();
	}

	private ProjectConfigProvider(String applicationId, ResourceManager resourceManager, Iam iamClient) {
	    this.resourceManager = resourceManager;
	    this.iamClient = iamClient;
	    this.applicationId = applicationId;
	    this.project = resourceManager.get(applicationId);
	    if (project == null) {
	    	throw new IllegalStateException(
					"The 'Cloud Resource Manager' API is probably not enabled in this project, " +
							"please check in the GCP console");
		}
	    log.info("Project number for '" + applicationId + "' is " + project.getProjectNumber());
    }

    public Project getProject() {
        return project;
    }

    public ProjectConfig getCachedProjectConfig() {
        return mutableConfigCache.get();
    }

    public boolean isProjectClientId(String clientId) {
        if (getCachedProjectConfig().getServiceAccountClientIds().containsValue(clientId))
            return true;
        return project.getProjectNumber().toString().equals(extractProjectNumber(clientId));
    }

    //TODO implement retries
    private ProjectConfig getProjectConfig() {
	    ImmutableMap<Role, ImmutableSet<Identity>> roleBindings = ImmutableMap.copyOf(
			    Maps.transformValues(getIamBindingsCached(applicationId), ImmutableSet::copyOf));
	    ImmutableBiMap.Builder<String, String> serviceAccountClientIds = ImmutableBiMap.builder();
	    for (ServiceAccount serviceAccount : listServiceAccountsCached()) {
	        serviceAccountClientIds.put(serviceAccount.getEmail(), serviceAccount.getOauth2ClientId());
        }
	    ProjectConfig result = new ProjectConfig(roleBindings, serviceAccountClientIds.build());
        log.info("Loaded project config: " + result);
        return result;
    }

    private ImmutableMap<Role, Set<Identity>> getIamBindingsCached(String key) {
    	//Policy object can't be serialized, but we only need the bindings
        return (ImmutableMap<Role, Set<Identity>>) ObjectCache.get()
		        .getCachedSerializable(key, ImmutableMap.class, 
				        input -> ImmutableMap.copyOf(resourceManager.getPolicy(applicationId).getBindings()),TTL_IN_SECONDS);
    }

    private List<ServiceAccount> listServiceAccountsCached() {
        return ObjectCache.get()
		        .getCachedJson(applicationId, ListServiceAccountsResponse.class, input -> listServiceAccounts(), TTL_IN_SECONDS)
		        .getAccounts();
    }

	@SneakyThrows
	private ListServiceAccountsResponse listServiceAccounts() {
    	//TODO handle service account list exceeding default page size
		return iamClient.projects().serviceAccounts().list("projects/" + applicationId).execute();
	}

}
