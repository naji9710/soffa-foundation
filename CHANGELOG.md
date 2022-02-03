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
