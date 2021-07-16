package wolkenschloss.conventions

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.TempDir

class WebappPluginTest extends Specification {

    @TempDir File testProjectDir
    File settingsFile
    File buildFile
    File packageJsonFile
    File mainJsFile
    File indexHtmlFile
    File appVueFile

    def setup() {
        settingsFile = new File(testProjectDir, 'settings.gradle')
        buildFile = new File(testProjectDir, 'build.gradle')

        packageJsonFile = new File(testProjectDir, "package.json")
        packageJsonFile << '''\
        {
          "name": "webapp",
          "version": "0.1.0",
          "private": true,
          "scripts": {
            "serve": "vue-cli-service serve",
            "build": "vue-cli-service build"
          },
          "dependencies": {
            "vue": "^2.6.11"
          },
          "devDependencies": {
            "@vue/cli-service": "~4.5.0",
            "vue-template-compiler": "^2.6.11"
          }
        }
        '''


        indexHtmlFile = new File(testProjectDir, "public/index.html")
        indexHtmlFile.parentFile.mkdirs()
        indexHtmlFile << """\
        <!DOCTYPE html>
        <html>
          <body>
            <div id="app"></div>
          </body>
        </html>
        """

        mainJsFile = new File(testProjectDir, "src/main.js")
        mainJsFile.parentFile.mkdirs()
        mainJsFile << '''\
            import Vue from 'vue'
            import App from './App.vue'
            
            new Vue({
              render: h => h(App),
            }).$mount('#app')
        '''

        appVueFile = new File(testProjectDir, "src/App.vue")
        appVueFile << """\
            <template>
              <div id="app">Hello World</div>
            </template>
            
            <script>
            export default {
              name: 'App',
            }
            </script>
        """

        setup: "create gradle project"
        settingsFile << "rootProject.name = 'webapp-example'"

        given: "a build file using core-conventions Plugin"
        buildFile << """
        plugins {
          id 'wolkenschloss.conventions.webapp'
        }
        """
    }

    def "build task should build jar file"() {
        when: "running gradle :build task"
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("build")
                .withPluginClasspath()
                .build()

        then: "the outcome of task :build is SUCCESS"
        result.task(':vue').outcome == TaskOutcome.SUCCESS

        and: "the outcome of task :vue is SUCCESS"
        result.task(':vue').outcome == TaskOutcome.SUCCESS

        and: "webapp-example.jar exists"
        def jar = new File(testProjectDir, "build/libs/webapp-example.jar")
        jar.exists()
    }

    def "vue task should build the vue app"() {
        when: "running gradle :build task"
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("vue")
                .withPluginClasspath()
                .build()

        then: "the outcome of task :buildVueApp is SUCCESS"
        result.task(':vue').outcome == TaskOutcome.SUCCESS

        and: "output is written into META-INF"
        def dir = new File(testProjectDir, "build/classes/java/main/META-INF/resources")
        dir.exists()
        new File(dir, "index.html").isFile()
        new File(dir, "js").isDirectory()
    }
}
