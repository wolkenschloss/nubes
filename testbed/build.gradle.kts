import wolkenschloss.domain.CopyKubeConfig
import wolkenschloss.domain.DomainTasks
import wolkenschloss.gradle.docker.BuildImageTask
import wolkenschloss.gradle.docker.RunContainerTask
import java.nio.file.Paths
import org.gradle.api.logging.LogLevel
import com.sun.security.auth.module.UnixSystem
import wolkenschloss.gradle.ca.CreateTask

plugins {
    id("com.github.wolkenschloss.testbed")
    id("com.github.wolkenschloss.docker")
    id("com.github.wolkenschloss.ca")
}

defaultTasks("start")

testbed {
    base {
        name.set("ubuntu-20.04")
        url.set("https://cloud-images.ubuntu.com/focal/current/focal-server-cloudimg-amd64-disk-kvm.img")
    }

    domain {
        name.set("testbed")
        domainSuffix.set("wolkenschloss.local")
        hosts.addAll("dashboard", "registry")
    }

    pool {
        name.set("testbed")
    }

    host {
        callbackPort.set(9292)
    }

    failOnError.set(false)
}

tasks {
    val buildDomain = named<wolkenschloss.domain.BuildDomain>(DomainTasks.BUILD_DOMAIN_TASK_NAME)
    val copyKubeConfig = named<CopyKubeConfig>(DomainTasks.READ_KUBE_CONFIG_TASK_NAME)

    val testbed: wolkenschloss.TestbedExtension by project.extensions
    val userHome = Paths.get(System.getProperty("user.home"))

    withType(RunContainerTask::class) {
        mount {
            input {
                file {
                    source.set(copyKubeConfig.get().kubeConfigFile)
                    target.set(userHome.resolve(Paths.get(".kube/config")).toAbsolutePath().toString())
                }
                file {
                    source.set(buildDomain.get().knownHostsFile)
                    target.set(userHome.resolve(Paths.get(".ssh/known_hosts")).toAbsolutePath().toString())
                }
                file {
                    source.set(testbed.user.privateSshKeyFile)
                    target.set(
                        userHome.resolve(Paths.get(".ssh", testbed.user.privateSshKeyFile.asFile.get().name))
                            .toAbsolutePath().toString()
                    )
                }
                file {
                    source.set(buildDomain.get().hostsFile)
                    target.set("/etc/hosts")
                }
            }
        }
    }

    val buildClientImage by registering(BuildImageTask::class) {
        inputDir.set(layout.projectDirectory.dir("docker/client/"))
        tags.add("nubes/client:latest")
        imageId.set(layout.buildDirectory.file("images/client"))

        // Push to default ???
        args.put("UID", UnixSystem().uid.toString())
        args.put("GID", UnixSystem().gid.toString())
        args.put("UNAME", System.getProperty("user.name"))
    }

    withType(RunContainerTask::class) {
        imageId.convention(buildClientImage.get().imageId)
    }

    // TODO refactor - move to :testbed:status
    val opensslversion by registering(RunContainerTask::class) {
        logging.captureStandardOutput(LogLevel.QUIET)
        command.addAll("openssl", "version")
        doNotTrackState("Prints version info")
    }

    // TODO refactor - move to :testbed:status
    val kubeversion by registering(RunContainerTask::class) {
        logging.captureStandardOutput(LogLevel.QUIET)
        command.addAll("kubectl", "version", "--short")
        doNotTrackState("Prints version info")
    }

    val newRootCa by registering(CreateTask::class) {
    }

     // Aufteilen in:
    // 1. CA erstellen
    // 2. Cert-Manager installieren
    // 3. CA installieren
    // DockerRunTask durch neue Fassung ersetzen
    val createRootCa by registering(RunContainerTask::class) {

        val src = layout.projectDirectory.dir("src/ca")
        logging.captureStandardOutput(LogLevel.QUIET)

        mount {
            input {
                file {
                    source.set(src.file("ca.bash"))
                    target.set("/usr/local/bin/ca.bash")
                }
                file {
                    source.set(newRootCa.flatMap { it.privateKey })
                    target.set("/opt/app/ca.key")
                }
                file {
                    source.set(newRootCa.flatMap { it.certificate })
                    target.set("/opt/app/ca.crt")
                }
                file {
                    source.set(src.file("ca-issuer.yaml"))
                    target.set("/opt/app/ca-issuer.yaml")
                }
            }
        }

        command.addAll("ca.bash", "/mnt/app")
    }

    val applyCommonServices by registering(RunContainerTask::class) {

        val src = layout.projectDirectory.dir("src/common")
        imageId.set(buildClientImage.get().imageId)

        mount {
            input {
                file {
                    source.set(src.file("dashboard-ingress.yaml"))
                    target.set("/opt/app/dashboard-ingress.yaml")
                }
                file {
                    source.set(src.file("registry-ingress.yaml"))
                    target.set("/opt/app/registry-ingress.yaml")
                }
                file {
                    source.set(src.file("kustomization.yaml"))
                    target.set("/opt/app/kustomization.yaml")
                }
            }
        }

        command.addAll("kubectl", "apply", "-k", "/opt/app")
        logging.captureStandardOutput(LogLevel.QUIET)
        doNotTrackState("For side effects only")
    }

    val readRootCa by registering(RunContainerTask::class) {
        logging.captureStandardOutput(LogLevel.QUIET)
        command.addAll(
            "/bin/bash",
            "-c",
            "kubectl get secrets -n cert-manager nubes-ca -o 'go-template={{index .data \"tls.crt\"}}' | base64 -d | openssl x509"
        )
        doNotTrackState("For side effects only")
    }

    val getCertificate by registering(RunContainerTask::class) {
        logging.captureStandardOutput(LogLevel.QUIET)
        command.addAll(
            "/bin/bash",
            "-c",
            "kubectl get secrets -n container-registry registry-cert -o 'go-template={{index .data \"tls.crt\"}}' | base64 -d | openssl x509 -text"
        )
        doNotTrackState("For side effects only")
    }

    val reset by registering(RunContainerTask::class) {
        logging.captureStandardOutput(LogLevel.QUIET)
        val host = "${testbed.domain.name.get()}.${testbed.domain.domainSuffix.get()}"
        command.addAll("/bin/bash", "-c", "ssh $host microk8s reset")
        doNotTrackState("For side effects only")
    }

//    val status by registering(RunContainerTask::class) {
//        logging.captureStandardOutput(LogLevel.QUIET)
//        val host = "${testbed.domain.name.get()}.${testbed.domain.domainSuffix.get()}"
//        command.addAll("/bin/bash", "-c", "ssh $host microk8s status")
//        doNotTrackState("For side effects only")
//    }

    named("start") {
        dependsOn(createRootCa)
    }
}