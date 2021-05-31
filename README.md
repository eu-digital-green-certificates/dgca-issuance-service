<h1 align="center">
    EU Digital COVID Certificate Issuance Service
</h1>

<p align="center">
    <a href="https://sonarcloud.io/dashboard?id=eu-digital-green-certificates_dgca-issuance-service" title="Quality Gate Status"><img src="https://sonarcloud.io/api/project_badges/measure?project=eu-digital-green-certificates_dgca-issuance-service&metric=alert_status"></a>
    <a href="/../../commits/" title="Last Commit"><img src="https://img.shields.io/github/last-commit/eu-digital-green-certificates/dgca-issuance-service?style=flat"></a>
    <a href="/../../issues" title="Open Issues"><img src="https://img.shields.io/github/issues/eu-digital-green-certificates/dgca-issuance-service?style=flat"></a>
    <a href="./LICENSE" title="License"><img src="https://img.shields.io/badge/License-Apache%202.0-green.svg?style=flat"></a>
</p>

<p align="center">
  <a href="#about">About</a> •
  <a href="#development">Development</a> •
  <a href="#documentation">Documentation</a> •
  <a href="#support-and-feedback">Support</a> •
  <a href="#how-to-contribute">Contribute</a> •
  <a href="#contributors">Contributors</a> •
  <a href="#licensing">Licensing</a>
</p>

## About

This repository contains the source code of the EU Digital COVID Certificate Issuance Service.

The issuer backend is accessed by the [issuer web frontend](https://github.com/eu-digital-green-certificates/dgca-issuance-web) and the respective wallet apps ( [Android](https://github.com/eu-digital-green-certificates/dgca-wallet-app-android), [iOS](https://github.com/eu-digital-green-certificates/dgca-wallet-app-ios) ) of the same member state. The backend itself publishes its public keys to the [DGCG](https://github.com/eu-digital-green-certificates/dgc-gateway) where they can be distributed to other member states. Each member state hosts its own issuer backend. The main function of the backend is to provide services for creating and signing new green certificates. 

## Development

### Prerequisites

- [Open JDK 11](https://openjdk.java.net)
- [Maven](https://maven.apache.org)
- [Docker](https://www.docker.com)
- Authenticate to [Github Packages](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry)

#### Authenticating in to GitHub Packages

As some of the required libraries (and/or versions are pinned/available only from GitHub Packages) You need to authenticate
to [GitHub Packages](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry)
The following steps need to be followed

- Create [PAT](https://docs.github.com/en/github/authenticating-to-github/creating-a-personal-access-token) with scopes:
  - `read:packages` for downloading packages

##### GitHub Maven

- Copy/Augment `~/.m2/settings.xml` with the contents of `settings.xml` present in this repository
  - Replace `${app.packages.username}` with your github username
  - Replace `${app.packages.password}` with the generated PAT

##### GitHub Docker Registry

- Run `docker login docker.pkg.github.com/eu-digital-green-certificates` before running further docker commands.
  - Use your GitHub username as username
  - Use the generated PAT as password

### Build

Whether you cloned or downloaded the 'zipped' sources you will either find the sources in the chosen checkout-directory or get a zip file with the source code, which you can expand to a folder of your choice.

In either case open a terminal pointing to the directory you put the sources in. The local build process is described afterwards depending on the way you choose.

### Build with maven
* Check [settings.xml](settings.xml) in root folder and copy the servers to your own `~/.m2/settings.xml` to connect the GitHub repositories we use in our code. Provide your GitHub username and access token (see [GitHub Help](https://docs.github.com/en/github/authenticating-to-github/creating-a-personal-access-token)) under the variables suggested. 
* Run `mvn clean package` from the project root folder

### Run with docker
* Perform maven build as described above
* Run `docker-compose up` from the project root folder

After all containers have started you will be able to reach the application on your [local machine](http://localhost:8080/dgci/status) under port 8080.

## Documentation  

* [configuration manual](docs/configuration.md)
* [developing configuration](docs/dev_config.md)

## Support and feedback

The following channels are available for discussions, feedback, and support requests:

| Type                     | Channel                                                |
| ------------------------ | ------------------------------------------------------ |
| **Issues**    | <a href="/../../issues" title="Open Issues"><img src="https://img.shields.io/github/issues/eu-digital-green-certificates/dgca-issuance-service?style=flat"></a>  |
| **Other requests**    | <a href="mailto:opensource@telekom.de" title="Email DGC Team"><img src="https://img.shields.io/badge/email-DGC%20team-green?logo=mail.ru&style=flat-square&logoColor=white"></a>   |

## How to contribute  

Contribution and feedback is encouraged and always welcome. For more information about how to contribute, the project structure, as well as additional contribution information, see our [Contribution Guidelines](./CONTRIBUTING.md). By participating in this project, you agree to abide by its [Code of Conduct](./CODE_OF_CONDUCT.md) at all times.

## Contributors  

Our commitment to open source means that we are enabling -in fact encouraging- all interested parties to contribute and become part of its developer community.

## Licensing

Copyright (C) 2021 T-Systems International GmbH and all other contributors

Licensed under the **Apache License, Version 2.0** (the "License"); you may not use this file except in compliance with the License.

You may obtain a copy of the License at https://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the [LICENSE](./LICENSE) for the specific language governing permissions and limitations under the License.
