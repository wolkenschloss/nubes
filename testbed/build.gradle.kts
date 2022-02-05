import wolkenschloss.DockerRunTask
import wolkenschloss.domain.CopyKubeConfig
import wolkenschloss.DockerBuildTask
import wolkenschloss.domain.DomainTasks
import com.sun.security.auth.module.UnixSystem
import java.nio.file.Paths


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
}

tasks {
    val buildDomain = named<wolkenschloss.domain.BuildDomain>(DomainTasks.BUILD_DOMAIN_TASK_NAME)
    val copyKubeConfig = named<CopyKubeConfig>(DomainTasks.READ_KUBE_CONFIG_TASK_NAME)

    val testbed: wolkenschloss.TestbedExtension by project.extensions
    val userHome = Paths.get(System.getProperty("user.home"))

    withType(DockerRunTask::class) {
        this.volumes {
            input {
                source.set(copyKubeConfig.get().kubeConfigFile)
                target.set(userHome.resolve(Paths.get(".kube/config")).toAbsolutePath().toString())
            }
            input {
                source.set(buildDomain.get().knownHostsFile)
                target.set(userHome.resolve(Paths.get(".ssh/known_hosts")).toAbsolutePath().toString())
            }
            input {
                source.set(testbed.user.privateSshKeyFile)
                target.set(userHome.resolve(Paths.get(".ssh", testbed.user.privateSshKeyFile.asFile.get().name)).toAbsolutePath().toString())
            }
            input {
                source.set(buildDomain.get().hostsFile)
                target.set("/etc/hosts")
            }
        }
    }

    val buildClientImage by registering(DockerBuildTask::class) {
        inputDir.set(layout.projectDirectory.dir("docker/client/"))
        tags.add("nubes/client:latest")
        image.set(layout.buildDirectory.file("images/client"))
        // Push to defaults
        userName.set(System.getProperty("user.name"))
        uid.set(UnixSystem().uid)
        gid.set(UnixSystem().gid)
    }

    withType(DockerRunTask::class) {
        image.convention(buildClientImage.get().image)
    }

    val opensslversion by registering(DockerRunTask::class) {
        containerLogLevel.set(LogLevel.QUIET)
        cmd.addAll("openssl", "version")
        doNotTrackState("Prints version info")
    }

    val kubeversion by registering(DockerRunTask::class) {
        cmd.addAll("kubectl", "version", "--short")
        containerLogLevel.set(LogLevel.QUIET)
        doNotTrackState("Prints version info")
    }

    val destroyRootCa by registering(DockerRunTask::class) {
        val home = System.getProperty("user.home")
        val userDataDir = System.getenv().getOrDefault("XDG_DATA_HOME", "$home/.local/share")
        val userPkiDir = Paths.get(userDataDir, "testbed")

        containerLogLevel.set(LogLevel.QUIET)
        volumes {
            output {
                source.set(userPkiDir.toFile())
                target.set("/mnt/app/testbed")
            }
        }

        cmd.addAll("rm", "-Rf", "/mnt/app/testbed/ca")
    }

    val createRootCa by registering(DockerRunTask::class) {

        val home = System.getProperty("user.home")
        val userDataDir = System.getenv().getOrDefault("XDG_DATA_HOME", "$home/.local/share")
        val userPkiDir = Paths.get(userDataDir, "testbed", "ca")

        val src = layout.projectDirectory.dir("src/ca")
        containerLogLevel.set(LogLevel.INFO)
        volumes {
            input {
                source.set(src.file("ca.bash"))
                target.set("/usr/local/bin/ca.bash")
            }

            input {
                source.set(src.file("ca.conf"))
                target.set("/opt/app/ca.conf")
            }

            input {
                source.set(src.file("ca-issuer.yaml"))
                target.set("/opt/app/ca-issuer.yaml")
            }

            output {
                source.set(userPkiDir.toFile())
                target.set("/mnt/app/ca")
            }
        }

        cmd.addAll("ca.bash", "/mnt/app/ca")

        doLast {
            logger.quiet("# To complete installation of testbed execute following tasks:")
            logger.quiet("# Copy and install CA with")
            logger.quiet("sudo cp ${userPkiDir.toAbsolutePath()}/ca.crt /usr/local/share/ca-certificates")
            logger.quiet("sudo update-ca-certificates")
            logger.quiet("# Add entry for local DNS lookup:")
            logger.quiet("sudo nano /etc/hosts")
            logger.quiet("# restart systemd resolver")
            logger.quiet("sudo systemctl restart systemd-resolved")
        }
    }

    val applyCommonServices by registering(DockerRunTask::class) {

        val src = layout.projectDirectory.dir("src/common")
        image.set(buildClientImage.get().image)

        volumes {

            input {
                source.set(src.file("dashboard-ingress.yaml"))
                target.set("/opt/app/dashboard-ingress.yaml")
            }

            input {
                source.set(src.file("registry-ingress.yaml"))
                target.set("/opt/app/registry-ingress.yaml")
            }

            input {
                source.set(src.file("kustomization.yaml"))
                target.set("/opt/app/kustomization.yaml")
            }

            cmd.addAll("kubectl", "apply", "-k", "/opt/app")
        }
    }

    val readRootCa by registering(DockerRunTask::class) {
        containerLogLevel.set(LogLevel.QUIET)
        cmd.addAll("/bin/bash", "-c", "kubectl get secrets -n cert-manager nubes-ca -o 'go-template={{index .data \"tls.crt\"}}' | base64 -d | openssl x509")
        doNotTrackState("For side effects only")
    }

    val getCertificate by registering(DockerRunTask::class) {
        containerLogLevel.set(LogLevel.QUIET)
        cmd.addAll("/bin/bash", "-c", "kubectl get secrets -n container-registry registry-cert -o 'go-template={{index .data \"tls.crt\"}}' | base64 -d | openssl x509 -text")
        doNotTrackState("For side effects only")
    }

    val reset by registering(DockerRunTask::class) {
        containerLogLevel.set(LogLevel.QUIET)
        val host = "${testbed.domain.name.get()}.${testbed.domain.domainSuffix.get()}"

        cmd.addAll("/bin/bash", "-c", "ssh $host microk8s status")
        doNotTrackState("For side effects only")
    }

    named("start") {
        dependsOn(createRootCa)
    }
}