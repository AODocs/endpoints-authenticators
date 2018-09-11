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

import com.aodocs.endpoints.auth.AppEngineTest;
import com.google.api.client.googleapis.util.Utils;
import com.google.api.services.iam.v1.Iam;
import com.google.api.services.iam.v1.model.ListServiceAccountsResponse;
import com.google.appengine.api.utils.SystemProperty;
import com.google.cloud.Identity;
import com.google.cloud.Policy;
import com.google.cloud.Role;
import com.google.cloud.resourcemanager.Project;
import com.google.cloud.resourcemanager.ProjectInfo;
import com.google.cloud.resourcemanager.ResourceManager;
import com.google.cloud.resourcemanager.testing.LocalResourceManagerHelper;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import lombok.SneakyThrows;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by Clement on 01/08/2016.
 */
public class ProjectConfigProviderTest extends AppEngineTest {

    private LocalResourceManagerHelper helper = LocalResourceManagerHelper.create();

    private Project project;

    @Before
    public void setUp() throws IOException {
        helper.start();
        final ResourceManager resourceManager = helper.getOptions().getService();
        final String appId = SystemProperty.applicationId.get();
        project = resourceManager.create(ProjectInfo.newBuilder(appId).build());
        resourceManager.replacePolicy(appId, Policy.newBuilder()
                .addIdentity(Role.of("someRole"), Identity.user("user@example.com"))
                .build());
        final Iam iam = mock(Iam.class, RETURNS_DEEP_STUBS);
        when(iam.projects().serviceAccounts().list("projects/" + appId).execute())
                .thenReturn(loadJson(ListServiceAccountsResponse.class));
        ProjectConfigProvider.override(appId, resourceManager, iam);
    }

    @SneakyThrows
    <T> T loadJson(Class<T> clazz) {
        final URL resource = Resources.getResource(ProjectConfigProviderTest.class, clazz.getSimpleName() + ".json");
        try (InputStream inputStream = resource.openStream()) {
            return Utils.getDefaultJsonFactory().createJsonParser(inputStream).parse(clazz);
        }
    }

    @After
    public void tearDown() {
        helper.stop();
    }

    @Test
    public void getProjectNumber() {
        long projectNumber = ProjectConfigProvider.get().getProject().getProjectNumber();
        assertEquals(project.getProjectNumber().longValue(), projectNumber);
    }

    @Test
    public void isProjectClientId() {
        //malformed client ids
        assertFalse(ProjectConfigProvider.get().isProjectClientId("428563709008"));
        assertFalse(ProjectConfigProvider.get().isProjectClientId(""));
        assertFalse(ProjectConfigProvider.get().isProjectClientId("anystring"));
        assertFalse(ProjectConfigProvider.get().isProjectClientId("42856370900a.apps.googleusercontent.com"));
        assertFalse(ProjectConfigProvider.get().isProjectClientId("428563709008sc2oogg2l01bag31ercne0mvi4v0ffvv.apps.googleusercontent.com"));

        //client ids belonging to project
        assertTrue(ProjectConfigProvider.get().isProjectClientId("428563709008-vvh1k92tpns1ab8qnhum5fetmk4iir47.apps.googleusercontent.com")); //old service account id
        assertTrue(ProjectConfigProvider.get().isProjectClientId("114047436807976998329")); //new service account id

        //client ids non belonging
        assertFalse(ProjectConfigProvider.get().isProjectClientId("114047436807976998328"));
        assertFalse(ProjectConfigProvider.get().isProjectClientId("528563709008-sc2oogg2l01bag31ercne0mvi4v0ffvv.apps.googleusercontent.com"));
    }

    @Test
	public void getCachedProjectConfig() {
        final ProjectConfigProvider.ProjectConfig projectConfig = ProjectConfigProvider.get().getCachedProjectConfig();
        final ImmutableSet<String> roles = projectConfig.getRolesFor("user@example.com");
        assertTrue(roles.contains("someRole"));
        assertEquals("428563709008-vvh1k92tpns1ab8qnhum5fetmk4iir47.apps.googleusercontent.com",
                projectConfig.getServiceAccountClientIds()
                        .get("428563709008-vvh1k92tpns1ab8qnhum5fetmk4iir47@developer.gserviceaccount.com"));
    }

}
