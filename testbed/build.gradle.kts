import family.haschka.wolkenschloss.gradle.ca.ServerCertificate
import family.haschka.wolkenschloss.gradle.testbed.Apply
import family.haschka.wolkenschloss.gradle.testbed.domain.DomainExtension
import family.haschka.wolkenschloss.gradle.testbed.domain.DomainTasks

plugins {
    id("family.haschka.wolkenschloss.testbed")
}

defaultTasks("start")

testbed {
    domain {
        name.set("testbed")
        domainSuffix.set(System.getProperty(DomainExtension.DOMAIN_SUFFIX_PROPERTY))
        hosts.addAll("dashboard", "registry", "grafana", "prometheus", "cookbook", "linkerd", "dex")
    }
}


tasks {

    register<ServerCertificate>("localhost") {
        subjectAlternativeNames.set(listOf(
            ServerCertificate.dnsName("localhost"),
            ServerCertificate.ipAddress("127.0.0.1")
        ))
    }

    start {
        dependsOn(DomainTasks.COPY_KUBE_CONFIG_TASK_NAME)
    }

    register<Apply>("staging") {
        group = "client"
        description = "apply staging overlay"
        logging.captureStandardOutput(LogLevel.QUIET)
    }
}