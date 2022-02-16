import wolkenschloss.gradle.docker.BuildImageTask
import wolkenschloss.gradle.docker.RunContainerTask
import wolkenschloss.gradle.testbed.domain.BuildDomain
import wolkenschloss.gradle.testbed.domain.DomainTasks
import wolkenschloss.gradle.testbed.domain.PushImage
import wolkenschloss.gradle.testbed.domain.CopyKubeConfig
import java.nio.file.Paths
import org.gradle.api.logging.LogLevel
import com.sun.security.auth.module.UnixSystem
import wolkenschloss.gradle.ca.CreateTask
import wolkenschloss.gradle.testbed.TestbedExtension
import wolkenschloss.gradle.testbed.domain.DomainExtension

plugins {
    id("com.github.wolkenschloss.testbed")
    id("com.github.wolkenschloss.docker")
}

defaultTasks("start")


testbed {
    base {
        name.set("ubuntu-20.04")
        url.set("https://cloud-images.ubuntu.com/focal/current/focal-server-cloudimg-amd64-disk-kvm.img")
    }

    domain {
        name.set("testbed")
        domainSuffix.set(System.getProperty(DomainExtension.DOMAIN_SUFFIX_PROPERTY))
        hosts.addAll("dashboard", "registry", "grafana", "prometheus")
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
    val buildDomain = named<BuildDomain>(DomainTasks.BUILD_DOMAIN_TASK_NAME)
    val copyKubeConfig = named<CopyKubeConfig>(DomainTasks.READ_KUBE_CONFIG_TASK_NAME)

    val testbed: TestbedExtension by project.extensions
    val userHome = Paths.get(System.getProperty("user.home"))


    val ca by existing(CreateTask::class)

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
                file {
                    source.set(ca.flatMap { it.certificate })
                    target.set("/usr/local/share/ca-certificates/ca.crt")
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



    val info by registering(RunContainerTask::class) {
        logging.captureStandardOutput(LogLevel.QUIET)
        command.addAll("info.bash")
        doNotTrackState("Prints testbed client info")
    }

    val installCertManager by registering(RunContainerTask::class) {

        val src = layout.projectDirectory.dir("src/ca")
        logging.captureStandardOutput(LogLevel.INFO)

        mount {
            input {
                file {
                    source.set(src.file("ca.bash"))
                    target.set("/usr/local/bin/ca.bash")
                }
                file {
                    source.set(ca.flatMap { it.privateKey })
                    target.set("/opt/app/ca.key")
                }
                file {
                    source.set(ca.flatMap { it.certificate })
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
                directory {
                    source.set(src)
                    target.set("/opt/app")
                }
            }
        }

        command.addAll("kubectl", "apply", "-k", "/opt/app")
        logging.captureStandardOutput(LogLevel.QUIET)
        doNotTrackState("For side effects only")
        dependsOn(installCertManager)
    }

    val deployCommonImages by registering(PushImage::class) {
        images.put("mongo:4.0.10", "mongo:4.0.10")
        images.put("hello-world", "hello-world")
        dependsOn(applyCommonServices)
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

    val allCerts by registering(RunContainerTask::class) {
        logging.captureStandardOutput(LogLevel.QUIET)
        command.addAll(
            "/bin/bash",
            "-c",
            "kubectl get secrets --all-namespaces --field-selector type=kubernetes.io/tls -o 'go-template={{index .data \"tls.crt\"}}'"
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

    named("start") {
        dependsOn(applyCommonServices)
    }
}