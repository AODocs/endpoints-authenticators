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

import java.util.Optional;
import java.util.function.Supplier;

/**
 * First try to get Flex env variables, then try to get Standard env properties.
 * Can work on Flex without the GAE SDK in the classpath.
 */
public class AppengineHelper {

  private final static Supplier<String> VERSION_SUPPLIER = () -> com.google.appengine.api.modules.ModulesServiceFactory.getModulesService().getCurrentVersion();
  private final static Supplier<String> SERVICE_SUPPLIER = () -> com.google.appengine.api.modules.ModulesServiceFactory.getModulesService().getCurrentModule();


  public static String getApplicationId() {
    return Optional.ofNullable(System.getenv("GOOGLE_CLOUD_PROJECT")).orElseGet(() -> System.getProperty("com.google.appengine.application.id"));
  }

  public static String getCurrentVersion() {
    return Optional.ofNullable(System.getenv("GAE_VERSION")).orElseGet(VERSION_SUPPLIER);
  }

  public static String getCurrentService() {
    return Optional.ofNullable(System.getenv("GAE_SERVICE")).orElseGet(SERVICE_SUPPLIER);
  }

  public static boolean isRunningOnAppengine() {
    return isRunningOnAppengineStandard() || isRunningOnAppengineFlex();
  }

  public static boolean isRunningOnAppengineStandard() {
    return System.getProperty("com.google.appengine.runtime.environment") != null;
  }

  public static boolean isRunningOnAppengineFlex() {
    return System.getenv("GOOGLE_CLOUD_PROJECT") != null;
  }

}
