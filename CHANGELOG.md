## 1.7.0 (February 16, 2022) - Breaking changes 

* Refactoring
* Separation of concerns
* Modularity
* Improvements and stability

## 1.5.10 (February 14, 2022)

* Improvements and stability

## 1.5.7 (February 13, 2022)

* Improvements and stability

## 1.5.5 (February 13, 2022)

* Improvements and stability
 
## 1.5.2 (February 12, 2022)

IMPROVEMENTS:

* Database Health Indicator

## 1.5.1 (February 12, 2022) - Breaking changes

* AuthManager improvements
* Refactoring
 
## 1.5.0 (February 11, 2022) - Breaking changes

* Actions are now Operations
* Code cleanup
* Netflix DGS integration 

## 1.4.0 (February 10, 2022)

* Database migrations are applied after initial startup
* Customer HealthIndicator added for database migration
* Database distributed lock 

## 1.3.9 (February 09, 2022)

* Nats.io integration for distributed binary messages
* Binary API Client added
* MetricsRegistry integration
* [Breaking] Refactoring

## 1.3.8 (February 08, 2022)

* `app.openapi.access` added to change the security level of the swagger endpoints
* `/health` alias added 

## 1.3.6 (February 08, 2022)

* Request should stop when authorization token is invalid
 
## 1.3.5 (February 07, 2022)

* Improvements and stability
 
## 1.3.4 (February 04, 2022)

* Expose `actuator/prometheus` endpoint.
* Dependencies update

## 1.3.3 (February 04, 2022)

IMPROVEMENTS:

* Json is enabled only with active profile "json-logs"
* Env variable `LOGGING_FILE_NAME` is now available to customize the log file name (when enabled)

 ## 1.3.2 (February 03, 2022)

IMPROVEMENTS:

* Consul integration
* Improvements and stability
 
## 1.3.1 (February 03, 2022)

IMPROVEMENTS:

* Dependencies updates
* Improvements and stability

## 1.3.0 (February 02, 2022)

BREAKING CHANGES:

* Datasources config structure:
```
app.db:
  tables-prefix: 
  datasources:
    default:
      url: h2://mem/test_default
      migration: true | or filename
      syslog: true|false
```
* Example: soffa-foundation-test-app/src/test/resources/application-test.yml

## 1.2.11 (January 27, 2022)

IMPROVEMENTS:

* Validation fixed (conflict between HibernateValidator and CheckerFramework).
* Basic auth security scheme added
* `RandomUtil` classes added

## 1.2.8 (January 10, 2022)

IMPROVEMENTS:

* Concurrency when dispatching events with and without explicit tenantId
* Implicit SysLog migrations
* Dedicated rabbitmq springboot profile: `foundation-amqp`

## 1.2.7 (January 07, 2022)

IMPROVEMENTS:

* Json expectations added to HttpExpect
* SQL table prefixes
