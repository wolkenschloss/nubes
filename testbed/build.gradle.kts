import wolkenschloss.gradle.ca.ServerCertificate
import wolkenschloss.gradle.testbed.Apply
import wolkenschloss.gradle.testbed.domain.DomainExtension
import wolkenschloss.gradle.testbed.domain.DomainTasks

plugins {
    id("com.github.wolkenschloss.testbed")
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
            ServerCertificate.DnsName("localhost"),
            ServerCertificate.IpAddress("127.0.0.1")
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