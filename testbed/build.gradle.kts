import wolkenschloss.gradle.ca.CreateTask
import wolkenschloss.gradle.docker.RunContainerTask
import wolkenschloss.gradle.testbed.TestbedExtension
import wolkenschloss.gradle.testbed.domain.*
import java.nio.file.Paths

plugins {
    id("com.github.wolkenschloss.testbed")
    id("com.github.wolkenschloss.docker")
}

defaultTasks("start")


testbed {
    domain {
        name.set("testbed")
        domainSuffix.set(System.getProperty(DomainExtension.DOMAIN_SUFFIX_PROPERTY))
        hosts.addAll("dashboard", "registry", "grafana", "prometheus")
    }
}

tasks {
    val buildDomain = named<BuildDomain>(DomainTasks.BUILD_DOMAIN_TASK_NAME)
    val copyKubeConfig = named<CopyKubeConfig>(DomainTasks.READ_KUBE_CONFIG_TASK_NAME)

    val testbed: TestbedExtension by project.extensions

    val ca by existing(CreateTask::class)

    val multipass = listOf("multipass", "exec", testbed.domain.name.get(), "--")
    val kubectl = multipass + listOf("microk8s", "kubectl")

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


    val deployCommonImages by registering(PushImage::class) {
        images.put("mongo:4.0.10", "mongo:4.0.10")
        images.put("hello-world", "hello-world")
//        dependsOn(applyCommonServices, waitForRegistry)
    }

    val readRootCa by registering(Exec::class) {
        logging.captureStandardOutput(LogLevel.QUIET)
        commandLine = multipass + listOf(
            "/bin/bash",
            "-c",
            "microk8s kubectl get secrets -n cert-manager nubes-ca -o $'go-template={{index .data \\\"tls.crt\\\"}}' | base64 -d | openssl x509 -text"
        )
        doNotTrackState("For side effects only")
    }

    val allCerts by registering(Exec::class) {
        logging.captureStandardOutput(LogLevel.QUIET)
        commandLine = multipass + listOf(
            "/bin/bash",
            "-c",
            "microk8s kubectl get secrets --all-namespaces --field-selector type=kubernetes.io/tls -o $'go-template={{index .data \\\"tls.crt\\\"}}'"
            // | base64 -d | openssl x509 -text
        )
        doNotTrackState("For side effects only")
    }

    val certs by registering(RunContainerTask::class) {
        logging.captureStandardOutput(LogLevel.QUIET)
        command.addAll(
            "/bin/bash", "-c",
            "kubectl get secrets --all-namespaces --field-selector type=kubernetes.io/tls -o go-template='{{range .items}}{{index .data \"tls.crt\"}}{{\"\\n\"}}{{end}}' | base64 -d |openssl storeutl -noout -text /dev/stdin"
            // ubectl get secrets --all-namespaces --field-selector type=kubernetes.io/tls -o go-template='{{range .items}}{{index .data "tls.crt"}}{{"\n"}}{{end}}' | base64 -d |openssl storeutl -noout -text /dev/stdin | awk '/Issuer:/{printf $NF"\n"} /Subject: C=/{printf $NF"\n"} /DNS:/{x=gsub(/ *DNS:/, ""); printf "SANS=" $0"\n"}'
//            "kubectl get secrets --field-selector type=kubernetes.io/tls --all-namespaces"
        )
        doNotTrackState("For side effects only")
    }

    val reset by registering(RunContainerTask::class) {
        logging.captureStandardOutput(LogLevel.QUIET)
        val host = "${testbed.domain.name.get()}.${testbed.domain.domainSuffix.get()}"
        command.addAll("/bin/bash", "-c", "ssh $host microk8s reset")
        doNotTrackState("For side effects only")
    }

    // NEU:
    val applyCommonServices by registering(DefaultTask::class) {
        logging.captureStandardOutput(LogLevel.QUIET)

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

        doNotTrackState("For side effects only")
    }

    val createSecret by registering(DefaultTask::class) {
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

        // TODO: Die Erstellung des Secrets kann fehlschlagen, dann muss umount trotzdem ausgeführt werden.
        project.exec {
            commandLine = kubectl + listOf(
                "create", "secret", "tls", "nubes-ca",
                "--key", "/home/ubuntu/ca/ca.key",
                "--cert", "/home/ubuntu/ca/ca.crt",
                "-n", "cert-manager"
            )
        }

        project.exec {
            commandLine = listOf("multipass", "umount", "${testbed.domain.name.get()}:/home/ubuntu/ca")
        }
    }

    named("start") {
        dependsOn(deployCommonImages)
    }
}