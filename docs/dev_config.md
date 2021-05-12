# Configuring for Developing

# Build
The application use maven as build system.
Some maven dependencies are github maven registry packages. The access to them need to be configured
using github access token.
See file settings.xml and consult the [gitub maven registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry)

This 2 maven repository are used as dependencies (see also pom.xml)
* https://github.com/eu-digital-green-certificates/dgc-lib
* https://github.com/ehn-digital-green-development/hcert-kotlin

# Running frontend and backend in dev mode

Start dgca-issuance-service in your favorite java IDE.

    Main class: eu.europa.ec.dgc.issuance.DgcIssuanceApplication

Pass following program argument to adapt endpoint prefix for need of dgca-issuance-web

    --server.servlet.context-path=/dgca-issuance-service

The server starts on port 8080. See log output.

Start [dgca-issuance-web](https://github.com/eu-digital-green-certificates/dgca-issuance-web)
by using 

    yarn start

The frontend starts in developing mode and is available on http://localhost:3000.
See frontend node developing server forward calls to backend (see package.json proxy entry)

You can use the service by REST client as Postman or use frontend directly.
You may also use swagger-ui

* [The swagger ui (localhost)](http://localhost:8080/dgca-issuance-service/swagger)
* [Open API endpoint (localhost)](http://localhost:8080/dgca-issuance-service/api/docs)



