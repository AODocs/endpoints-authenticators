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
package com.aodocs.endpoints.auth.init;

import com.google.api.server.spi.config.*;
import com.google.api.server.spi.request.Auth;
import lombok.extern.java.Log;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Scans the classpath for API classes, and initializes all custom authenticators
 */
@Log
public class EagerAuthenticatorInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Reflections reflections = new Reflections(sce.getServletContext().getInitParameter("apiPackagePrefix"),
                new MethodAnnotationsScanner(), new TypeAnnotationsScanner(), new SubTypesScanner());
        try {
            Set<Class<? extends Authenticator>> uniqueAuthenticatorClasses =
                    Stream.concat(Stream.concat(
                            getAuthenticators(reflections.getMethodsAnnotatedWith(ApiMethod.class), ApiMethod.class),
                            getAuthenticators(reflections.getTypesAnnotatedWith(Api.class), Api.class)),
                            getAuthenticators(reflections.getTypesAnnotatedWith(ApiClass.class), ApiClass.class))
                            .collect(Collectors.toSet());
            long authenticatorCount = uniqueAuthenticatorClasses.stream()
		            .map(Auth::instantiateAuthenticator)
                    .filter(Objects::nonNull)
                    .count();
            log.log(Level.INFO, "Initialized " + authenticatorCount + " authenticators");
        } catch (Exception e) {
            log.log(Level.INFO, "Cannot eagerly initialize authenticators", e);
        }
    }

    private Stream<Class<? extends Authenticator>> getAuthenticators(
            Set<? extends AnnotatedElement> annotatedElements, final Class<? extends Annotation> annotationClass) {

        return annotatedElements.stream().flatMap(input -> {
                    try {
                        Annotation annotation = input.getAnnotation(annotationClass);
                        Method authenticatorsMethod = annotationClass.getMethod("authenticators");
                        Class<? extends Authenticator>[] authenticators
                                = (Class<? extends Authenticator>[]) authenticatorsMethod.invoke(annotation);
                        return Stream.of(authenticators);
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        log.log(Level.INFO, "Cannot read annotation on " + input, e);
                        return Stream.empty();
                    }
                })
                .filter(input -> !input.isInterface())
                .filter(input -> input.getAnnotation(Singleton.class) != null)
                .filter(Objects::nonNull);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        //nothing to do
    }
}
