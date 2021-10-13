/*-
 * #%L
 * Extended authenticators for Cloud Endpoints v2
 * ---
 * Copyright (C) 2018 - 2021 AODocs (Altirnao Inc)
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
import com.google.cloud.resourcemanager.v3.Project;
import com.google.cloud.resourcemanager.v3.ProjectsClient;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;
import com.google.iam.v1.Binding;

import lombok.SneakyThrows;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by Clement on 01/08/2016.
 */
public class ProjectConfigProviderTest extends AppEngineTest {

    private static final String expectedProjectNumber = "1234567";
    private ProjectConfigProvider projectConfigProvider;

    @Before
    public void setUp() throws IOException {
        final String appId = SystemProperty.applicationId.get();
        String projectName = "projects/" + appId;
        //this instance has final methods, mocking needs mock-maker-inline
        //see src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker
        final ProjectsClient projectsClient = mock(ProjectsClient.class, RETURNS_DEEP_STUBS);
        when(projectsClient.getProject(projectName))
                .thenReturn(Project.newBuilder()
                        .setName("projects/" + expectedProjectNumber)
                        .build());
        when(projectsClient.getIamPolicy(projectName).getBindingsList())
                .thenReturn(Collections.singletonList(Binding.newBuilder()
                                .setRole("someRole")
                                .addMembers(Identity.user("user@example.com").strValue())
                                .build()));
        final Iam iam = mock(Iam.class, RETURNS_DEEP_STUBS);
        when(iam.projects().serviceAccounts().list(projectName).execute())
                .thenReturn(loadJson(ListServiceAccountsResponse.class));
        projectConfigProvider = new ProjectConfigProvider(appId) {
            @Override
            protected ProjectsClient getProjectsClient() {
                return projectsClient;
            }
            @Override
            protected Iam getIamClient() {
                return iam;
            }
        };
    }

    @SneakyThrows
    <T> T loadJson(Class<T> clazz) {
        final URL resource = Resources.getResource(ProjectConfigProviderTest.class, clazz.getSimpleName() + ".json");
        try (InputStream inputStream = resource.openStream()) {
            return Utils.getDefaultJsonFactory().createJsonParser(inputStream).parse(clazz);
        }
    }

    @Test
    public void getProjectNumber() {
        String projectNumber = projectConfigProvider.getProjectNumber();
        assertEquals(expectedProjectNumber, projectNumber);
    }

    @Test
    public void isProjectClientId() {
        //malformed client ids
        assertFalse(projectConfigProvider.isProjectClientId("428563709008"));
        assertFalse(projectConfigProvider.isProjectClientId(""));
        assertFalse(projectConfigProvider.isProjectClientId("anystring"));
        assertFalse(projectConfigProvider.isProjectClientId("42856370900a.apps.googleusercontent.com"));
        assertFalse(projectConfigProvider.isProjectClientId("428563709008sc2oogg2l01bag31ercne0mvi4v0ffvv.apps.googleusercontent.com"));

        //client ids belonging to project
        assertTrue(projectConfigProvider.isProjectClientId("428563709008-vvh1k92tpns1ab8qnhum5fetmk4iir47.apps.googleusercontent.com")); //old service account id
        assertTrue(projectConfigProvider.isProjectClientId("114047436807976998329")); //new service account id

        //client ids non belonging
        assertFalse(projectConfigProvider.isProjectClientId("114047436807976998328"));
        assertFalse(projectConfigProvider.isProjectClientId("528563709008-sc2oogg2l01bag31ercne0mvi4v0ffvv.apps.googleusercontent.com"));
    }

    @Test
	public void getCachedProjectConfig() {
        final ProjectConfigProvider.ProjectConfig projectConfig = projectConfigProvider.getCachedProjectConfig();
        final ImmutableSet<String> roles = projectConfig.getRolesFor("user@example.com");
        assertTrue(roles.contains("someRole"));
        assertEquals("428563709008-vvh1k92tpns1ab8qnhum5fetmk4iir47.apps.googleusercontent.com",
                projectConfig.getServiceAccountClientIds()
                        .get("428563709008-vvh1k92tpns1ab8qnhum5fetmk4iir47@developer.gserviceaccount.com"));
    }

}
