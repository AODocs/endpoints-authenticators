This project provides a  set of custom authenticators for the [Cloud Endpoints](https://cloud.google.com/endpoints/docs/frameworks/about-cloud-endpoints-frameworks) framework, allowing to control authentication much more precisely than the built-in authenticator mechanisms.

This is targeted to run on App Engine (either Standard or Flex) and was NOT tested on other environments (GCE or GKE).

# Features
- New strategies for authentication
  - Built-in client id checking is disabled by default, as it is deploy-time only
  - Allow client ids from the current Google Cloud project (including service accounts)
  - Allow runtime-configured lists of client ids or whole projects (excluding service accounts)
  - Use project roles (through IAM) as an authorization source
  - Request path, HTTP verb, query parameters and API version as an authorization criteria
  - Check token type (OAuth2 or JWT
  - Combine authenticators logically to create advanced strategies
  - Use a simple DSL (either in YAML or JSON format) to describe advanced strategies
- The configuration can be reloaded at runtime, using various dynamic sources
  - Cloud Storage files
  - Datastore entities
  - Classpath (deploy time only, no hot-reload)
  - Multiple sources can be merged

# Limitations
- Does not support authentication based on the AppEngine cookie
- Probably does not run on GCE / GKE / on-prem

# How to use

All the custom authenticators inherit from ExtendedAuthenticator, and provides an ExtendedUser object to implementations with much more information than the built-in User object.

To use a custom authenticator, nnotate your API with a custom [Authenticator](https://cloud.google.com/endpoints/docs/frameworks/java/javadoc/com/google/api/server/spi/config/Authenticator)) using the "authenticators" field of @Api, @ApiClass or @ApiMethod. You might want to set the "clientIds" field of these annotation to "*" to indicate the built-in check is disabled, but using a subclass of ExtendedAuthenticator will disable it anyway.

An Authenticator class must have a public parameter-less constructor and be a @Singleton, so you will probably have to create subclasses of the provided custom Authenticators if they require configuration.


