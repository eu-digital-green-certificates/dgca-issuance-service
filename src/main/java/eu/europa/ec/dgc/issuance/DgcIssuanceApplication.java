/*-
 * ---license-start
 * EU Digital Green Certificate Issuance Service / dgca-issuance-service
 * ---
 * Copyright (C) 2021 T-Systems International GmbH and all other contributors
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
 * ---license-end
 */

package eu.europa.ec.dgc.issuance;

import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import eu.europa.ec.dgc.issuance.config.IssuanceConfigProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import java.io.IOException;
import java.io.InputStream;
import org.openhim.mediator.engine.MediatorConfig;
import org.openhim.mediator.engine.MediatorServer;
import org.openhim.mediator.engine.RegistrationConfig;
import org.openhim.mediator.engine.RoutingTable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * The Application class.
 */
@SpringBootApplication
@EnableConfigurationProperties({IssuanceConfigProperties.class})
public class DgcIssuanceApplication extends SpringBootServletInitializer {

    /**
     * The main Method.
     *
     * @param args the args for the main method
     */
    public static void main(String[] args) throws Exception {

        //setup actor system
        final ActorSystem system = ActorSystem.create("mediator");
        //setup logger for main
        final LoggingAdapter log = Logging.getLogger(system, "main");

        //setup actors
        log.info("Initializing mediator actors...");

        MediatorConfig config = new MediatorConfig("my-mediator", "0.0.0.0", 8500);

        config.setCoreHost("localhost");
        config.setCoreAPIUsername("root@openhim.org");
        config.setCoreAPIPassword("test1234");
        MediatorConfig.SSLContext sslContext = new MediatorConfig.SSLContext(true);
        config.setSSLContext(sslContext);
        config.setHeartbeatsEnabled(true);

        RoutingTable routingTable = new RoutingTable();
        routingTable.addRoute("/ddcc/", MyActor.class);
        config.setRoutingTable(routingTable);

        InputStream regInfo =
            DgcIssuanceApplication.class.getClassLoader().getResourceAsStream("mediator-registration-info.json");
        RegistrationConfig regConfig = new RegistrationConfig(regInfo);
        config.setRegistrationConfig(regConfig);

        final MediatorServer server = new MediatorServer(system, config);

        //setup shutdown hook (will handle ctrl-c)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down mediator");
            server.stop();
            system.shutdown();
        }));

        log.info("Starting mediator server...");
        server.start();

        log.info("Mediator listening on 0.0.0.0:8500");

        SpringApplication.run(DgcIssuanceApplication.class, args);

        Thread.currentThread().join();
    }
}
