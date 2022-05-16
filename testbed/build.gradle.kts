import wolkenschloss.gradle.ca.ServerCertificate
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

val multipass = listOf("multipass", "exec", testbed.domain.name.get(), "--")
val docker = multipass + listOf("docker")
val kubectl = multipass + listOf("microk8s", "kubectl")

fun Project.mount(source: String, target: String, block: () -> Unit) {
    exec {
        commandLine = listOf("multipass", "mount", source, "${testbed.domain.name.get()}:${target}")
    }

    try {
        block()
    } finally {
        exec {
            commandLine = listOf("multipass", "umount", "${testbed.domain.name.get()}:${target}")
        }
    }
}


tasks {

    val localhost by registering(ServerCertificate::class) {
        subjectAlternativeNames.set(listOf(
            ServerCertificate.DnsName("localhost"),
            ServerCertificate.IpAddress("127.0.0.1")
        ))
    }

    val start by existing {
        dependsOn(DomainTasks.READ_KUBE_CONFIG_TASK_NAME)
    }

    val staging by registering(DefaultTask::class) {
        group = "client"
        description = "apply staging overlay"
        logging.captureStandardOutput(LogLevel.QUIET)
        doLast {
            project.mount(project.rootProject.layout.projectDirectory.asFile.absolutePath, "/home/ubuntu/nubes") {
                project.exec {
                    commandLine = docker + listOf("build", "-t", "nubes/generators/db-secret-generator", "/home/ubuntu/nubes/kustomize/db-secret-generator")
                }

                project.exec {
                    commandLine = multipass + listOf(
                        "/bin/bash",
                        "-c",
                        "microk8s kubectl kustomize --enable-alpha-plugins /home/ubuntu/nubes/overlays/staging/ | microk8s kubectl apply -f -"
                    )
                }
            }
        }
    }

    val kustomize by registering(DefaultTask::class) {
        group = "client"
        description = "run kubectl kustomize and print result"
        logging.captureStandardOutput(LogLevel.QUIET)
        doLast {
            project.mount(project.rootProject.layout.projectDirectory.asFile.absolutePath, "/home/ubuntu/nubes") {
                project.exec {
                    commandLine = docker + listOf("build", "-t", "nubes/generators/db-secret-generator", "/home/ubuntu/nubes/kustomize/db-secret-generator")
                }
                project.exec {
                    commandLine = kubectl + listOf("kustomize", "--enable-alpha-plugins", "/home/ubuntu/nubes/overlays/staging")
                }
            }
        }
    }
}