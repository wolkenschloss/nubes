import wolkenschloss.DockerRunTask
import wolkenschloss.domain.CopyKubeConfig
import wolkenschloss.DockerBuildTask
import wolkenschloss.domain.DomainTasks
import com.sun.security.auth.module.UnixSystem
import java.nio.file.Paths

plugins {
    id("wolkenschloss.testbed")
}

defaultTasks("start")

testbed {
    base {
        name.set("ubuntu-20.04")
        url.set("https://cloud-images.ubuntu.com/focal/current/focal-server-cloudimg-amd64-disk-kvm.img")
    }

    domain {
        name.set("testbed")
    }

    pool {
        name.set("testbed")
    }

    host {
        callbackPort.set(9292)
    }
}

tasks {
    val copyKubeConfig = named<CopyKubeConfig>(DomainTasks.READ_KUBE_CONFIG_TASK_NAME)

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

        cmd.addAll("rm", "-Rf", "/mnt/app/testbed/pki")
    }

    val createRootCa by registering(DockerRunTask::class) {

        val home = System.getProperty("user.home")
        val userDataDir = System.getenv().getOrDefault("XDG_DATA_HOME", "$home/.local/share")
        val userPkiDir = Paths.get(userDataDir, "testbed", "pki")

        val src = layout.projectDirectory.dir("src/pki")
        containerLogLevel.set(LogLevel.QUIET)
        volumes {
            input {
                source.set(src.file("pki.bash"))
                target.set("/usr/local/bin/pki.bash")
            }

            input {
                source.set(src.file("root-ca.conf"))
                target.set("/opt/app/root-ca.conf")
            }

            input {
                source.set(src.file("ca-issuer.yaml"))
                target.set("/opt/app/ca-issuer.yaml")
            }

            output {
                source.set(userPkiDir.toFile())
                target.set("/mnt/app/pki")
            }
        }

        cmd.addAll("pki.bash", "/mnt/app/pki")
    }

    val buildDomain = named<wolkenschloss.domain.BuildDomain>(DomainTasks.BUILD_DOMAIN_TASK_NAME)
    val testbed: wolkenschloss.TestbedExtension by project.extensions

    val userHome = Paths.get("/home", System.getProperty("user.name"))
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

    val installCommonServices by registering(DockerRunTask::class) {

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
        image.set(buildClientImage.get().image)
        containerLogLevel.set(LogLevel.QUIET)
        cmd.addAll("/bin/bash", "-c", "kubectl get secret -n cert-manager nubes-ca -o json | jq -r '.data.\"tls.crt\"' | base64 -d | openssl x509")
        doNotTrackState("Prints Root CA Certificate")
    }

    val reset by registering(DockerRunTask::class) {
        containerLogLevel.set(LogLevel.QUIET)
        cmd.addAll("/bin/bash", "-c", "ssh -v testbed.wolkenschloss.local microk8s status")
        cmd.addAll("/bin/bash", "-c", "ssh -v testbed.wolkenschloss.local microk8s reset")
        doNotTrackState("For side effects only")
    }

    named("start") {
        dependsOn(createRootCa)
    }
}