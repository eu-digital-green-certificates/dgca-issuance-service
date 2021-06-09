# Configuration Manual of Issuance Service

# Introduction
Issuance Service is developed as self-contained spring boot application.
The issuance application consist of 2 parts
 * dgca-issuance-service - this spring boot application. Backend application that serves services as REST Endpoints.
 * [dgca-issuance-web](https://github.com/eu-digital-green-certificates/dgca-issuance-web) - Web frontend programmed using React framework
The documentation here concern only dgca-issuance-service
The configuration of issuance service is done using [spring boot configuration capabilities](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config).
   
# Default Configuration
The default configuration (default profile) is defined by file src/main/resources/application.yml
It is part of application deployment and is used as default.
This default configuration is set to enable easy start of application out-of-the-box without additional dependencies but
it is not usable for productive environment and miss some functionality.
* usage of in-memory h2 database - all data are lost by restart
* use public test keystore for signing - see certs/test.jks
* no connection to dgc-gateway - the publish key endpoint is not usable

# Configuring Signing Keys for EDGC (European Green Certificate)
The issuance service needs private-public key to sign the EDGC.
The keys are stored in jks-keystore file and protected by password.
The private key is protected by additional password.
There are cert/test.jks file that are provided for testing purposes only.
You need to create own private key and keep it secret.
The issuance service may use only one private key to sign the message.

Following properties defines it (compare src/main/resources/application.yml)

```
issuance:
   keyStoreFile: certs/test.jks
   keyStorePassword: dgca
   certAlias: edgc_dev_ec
   privateKeyPassword: dgca
```

You may use the [Keystore Explorer](https://keystore-explorer.org/) to create jks keystore file and certificates.
Following key types are supported
* EC P-256 (for primary edgc algorihm)
* RSA 2048 bit (for secondary egdc algorithm)

For detailed informations see: https://ec.europa.eu/health/ehealth/covid-19_en

# Configuring EDGC Parameters
Following parameter configure the creation and handling of EDGC

```
issuance:
   dgciPrefix: dgci:V1:DE
   countryCode: DE
   tanExpirationHours: 2
   expiration:
      vaccination: 365
      recovery: 365
      test: 60
```

The dgciPrefix, countryCode and expiration are used to set up the EDGC fields.
The tanExpirationHours is used to expire first TAN for wallet claim process. 

# Configuring Database
The application needs a database to store dgci data and claim.
The default database is in-memory H2 database and is usable for development only.
In the spring profile "cloud" see src/main/resources/application-cloud.yml there are example postgres database configured.
Consult [spring boot manuals](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto-data-access) 

# Context File
Context file is used by wallet app for url pinning. It will be server at /context endpoint without any other modification.
If the context file is not configured and empty context json will be returned.

```
issuance:
   context: context.json
```

# Enabling and Disabling Endpoints

The endpoints are grouped by functions and can be enabled or disabled per configuration.
Default configuration for endpoints.

```
issuance:
  endpoints:
    frontendIssuing: true
    backendIssuing: false
    testTools: false
    wallet: true
    publishCert: true
    did: true
```

| group           | endpoints                 |
| ----------------|-------------------------- |
| frontendIssuing | `POST /dgci`<br/> `PUT /dgci/{id}` |
| backendIssuing | `PUT /dgci` |
| testTools | `POST /cert/dumpCBOR`<br> `POST /cert/decodeEGC`<br> `GET /cert/publicKey` |
| wallet | `POST /dgci/wallet/claim` |
| publishCert | `POST /dgci/certPublish` |
| did | `HEAD /dgci/{dgciHash}`<br/> `GET /dgci/{dgciHash}` |

  
# Configuring Connection to EDGC Gateway
The connection to EDGC is optional.
The application uses DGC Gateway connector from dgc-lib to configure and use the dgc-gateway. 
If enabled you may use PUT /dgci/certPublish to public the public signing key to EDGC Gateway.

For detailed information see:
[dgc-lib repositoy](https://github.com/eu-digital-green-certificates/dgc-lib)

There are no public free dgc-getway service therefore you will not find any connection parameters here

# Access Control for REST Endpoints

Overview of REST Connections and participating systems
![Issuance Service Overview](issuance-service-overview.svg)


Access point for issue creation (must be protected)
* /dgci/issue
* /dgci/issue/*

Access point to trigger public key publishing to EDGC Gateway (must be protected)
* /dgci/certPublish

Public Access point for wallet app
* /dgci/wallet/*
* /context
* GET /dgci/{dgciHash}

Developing/Test Endpoints
* /cert/*




