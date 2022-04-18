import wolkenschloss.gradle.testbed.TestbedExtension
import wolkenschloss.gradle.testbed.domain.BuildDomain
import wolkenschloss.gradle.testbed.domain.DomainExtension
import wolkenschloss.gradle.testbed.domain.DomainTasks
import wolkenschloss.gradle.ca.CreateTask

plugins {
    id("com.github.wolkenschloss.testbed")
}

defaultTasks("start")

testbed {
    domain {
        name.set("testbed")
        domainSuffix.set(System.getProperty(DomainExtension.DOMAIN_SUFFIX_PROPERTY))
        hosts.addAll("dashboard", "registry", "grafana", "prometheus", "cookbook", "linkerd")
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
    val buildDomain = named<BuildDomain>(DomainTasks.BUILD_DOMAIN_TASK_NAME)
    val testbed: TestbedExtension by project.extensions
    val ca by existing(CreateTask::class)

    val createSecret by registering(DefaultTask::class) {
        group = "server"
        description = "create wolkenschloss root CA secret"
        dependsOn(buildDomain, ca)
        logging.captureStandardOutput(LogLevel.QUIET)

        doLast {
            // TODO: benötigt laufende testbed Instanz
            // TODO: Verzeichnis kann bereits gemounted sein. Das ist aber kein Fehler.
            project.mount(wolkenschloss.gradle.testbed.Directories.certificateAuthorityHome.toAbsolutePath().toString(),"/home/ubuntu/ca") {
                val result = project.exec {
                    commandLine = kubectl + listOf(
                        "get", "secrets/nubes-ca", "-n", "cert-manager"
                    )
                    isIgnoreExitValue = true
                }

                if (result.exitValue != 0) {
                    logger.info("Creating new secret nubes-ca.")
                    project.exec {
                        commandLine = kubectl + listOf(
                            "create", "secret", "tls", "nubes-ca",
                            "--key", "/home/ubuntu/ca/ca.key",
                            "--cert", "/home/ubuntu/ca/ca.crt",
                            "-n", "cert-manager"
                        )
                    }
                } else {
                    logger.info("Secret nubes-ca already exists.")
                }
            }
        }
    }

    // TODO: Refactor - create Task apply in testbed plugin
    val applyCommonServices by registering(DefaultTask::class) {
        group = "server"
        description = "install ingress for cluster services (dashboard, registry)"

        logging.captureStandardOutput(LogLevel.QUIET)
        dependsOn(createSecret)
        doLast {
            project.exec {
                workingDir = project.layout.projectDirectory.asFile
                commandLine = listOf("multipass", "mount", ".", "${testbed.domain.name.get()}:/home/ubuntu/testbed")
            }

            project.exec {
                // TODO: benötigt mount
                commandLine = kubectl + listOf(
                    "apply",
                    "-k",
                    "/home/ubuntu/testbed/src/common"
                )
            }

            project.exec {
                workingDir = project.layout.projectDirectory.asFile
                commandLine = listOf("multipass", "umount", "${testbed.domain.name.get()}:/home/ubuntu/testbed")
            }
        }

        doNotTrackState("For side effects only")
    }

    val start by existing {
        dependsOn(applyCommonServices, DomainTasks.READ_KUBE_CONFIG_TASK_NAME)
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

        // Diese Abhängigkeit ist nicht so optimal.
        // Staging ist irgendwie abhängig von allen Service Projekten
//        dependsOn(start)
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