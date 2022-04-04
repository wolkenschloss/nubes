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
val kubectl = multipass + listOf("microk8s", "kubectl")

tasks {
    val buildDomain = named<BuildDomain>(DomainTasks.BUILD_DOMAIN_TASK_NAME)
    val testbed: TestbedExtension by project.extensions
    val ca by existing(CreateTask::class)

//    val readRootCa by registering(Exec::class) {
//        logging.captureStandardOutput(LogLevel.QUIET)
//        commandLine = multipass + listOf(
//            "/bin/bash",
//            "-c",
//            "microk8s kubectl get secrets -n cert-manager nubes-ca -o $'go-template={{index .data \\\"tls.crt\\\"}}' | base64 -d | openssl x509 -text"
//        )
//        doNotTrackState("For side effects only")
//    }
//
//    val allCerts by registering(Exec::class) {
//        logging.captureStandardOutput(LogLevel.QUIET)
//        commandLine = multipass + listOf(
//            "/bin/bash",
//            "-c",
//            "microk8s kubectl get secrets --all-namespaces --field-selector type=kubernetes.io/tls -o $'go-template={{index .data \\\"tls.crt\\\"}}'"
//            // | base64 -d | openssl x509 -text
//        )
//        doNotTrackState("For side effects only")
//    }
//
//    val certs by registering(RunContainerTask::class) {
//        logging.captureStandardOutput(LogLevel.QUIET)
//        command.addAll(
//            "/bin/bash", "-c",
//            "kubectl get secrets --all-namespaces --field-selector type=kubernetes.io/tls -o go-template='{{range .items}}{{index .data \"tls.crt\"}}{{\"\\n\"}}{{end}}' | base64 -d |openssl storeutl -noout -text /dev/stdin"
//            // ubectl get secrets --all-namespaces --field-selector type=kubernetes.io/tls -o go-template='{{range .items}}{{index .data "tls.crt"}}{{"\n"}}{{end}}' | base64 -d |openssl storeutl -noout -text /dev/stdin | awk '/Issuer:/{printf $NF"\n"} /Subject: C=/{printf $NF"\n"} /DNS:/{x=gsub(/ *DNS:/, ""); printf "SANS=" $0"\n"}'
////            "kubectl get secrets --field-selector type=kubernetes.io/tls --all-namespaces"
//        )
//        doNotTrackState("For side effects only")
//    }

    // NEU:

    val createSecret by registering(DefaultTask::class) {
        group = "server"
        description = "create wolkenschloss root CA secret"
        dependsOn(buildDomain, ca)
        logging.captureStandardOutput(LogLevel.QUIET)

        doLast {
            // TODO: benötigt laufende testbed Instanz
            // TODO: Verzeichnis kann bereits gemounted sein. Das ist aber kein Fehler.
            project.exec {
                commandLine = listOf(
                    "multipass",
                    "mount",
                    wolkenschloss.gradle.testbed.Directories.certificateAuthorityHome.toAbsolutePath().toString(),
                    "${testbed.domain.name.get()}:/home/ubuntu/ca"
                )
            }

            // check if secret exists
            val result = project.exec {
                commandLine = kubectl + listOf(
                    "get", "secrets/nubes-ca", "-n", "cert-manager"
                )
                isIgnoreExitValue = true
            }

            if (result.exitValue != 0) {
                logger.info("Creating new secret nubes-ca.")
                // TODO: Die Erstellung des Secrets kann fehlschlagen, dann muss umount trotzdem ausgeführt werden.
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

            project.exec {
                commandLine = listOf("multipass", "umount", "${testbed.domain.name.get()}:/home/ubuntu/ca")
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

    val waitForRegistry by registering(Exec::class) {
        doNotTrackState("For side effects only")
        logging.captureStandardOutput(LogLevel.QUIET)
        commandLine = kubectl + listOf(
            "rollout",
            "status",
            "deployment/registry",
            "--timeout=240s",
            "-n",
            "container-registry"
        )
    }

    val start by existing {
        dependsOn(applyCommonServices, waitForRegistry, DomainTasks.READ_KUBE_CONFIG_TASK_NAME)
    }

    val staging by registering(DefaultTask::class) {
        group = "client"
        description = "apply staging overlay"
        logging.captureStandardOutput(LogLevel.QUIET)
        doLast {
            project.exec {
                commandLine = listOf(
                    "multipass",
                    "mount",
                    project.rootProject.layout.projectDirectory.asFile.absolutePath,
                    "${testbed.domain.name.get()}:/home/ubuntu/nubes"
                )
            }

            project.exec {
                commandLine = kubectl + listOf("apply", "-k", "/home/ubuntu/nubes/overlays/staging")
            }

            project.exec {
                commandLine = listOf("multipass", "umount", "${testbed.domain.name.get()}:/home/ubuntu/nubes")
            }
        }

        // Diese Abhängigkeit ist nicht so optimal.
        // Staging ist irgendwie abhängig von allen Service Projekten
        dependsOn(start)
    }
}