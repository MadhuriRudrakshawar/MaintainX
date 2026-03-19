pipeline {
  agent any

  options {
    disableConcurrentBuilds()
    durabilityHint('PERFORMANCE_OPTIMIZED')
    skipDefaultCheckout(true)
  }

  parameters {
     booleanParam(
        name: 'RUN_UI_TESTS',
        defaultValue: true,
        description: 'Run Selenium UI tests'
     )
  }

  environment {
    GITHUB_TOKEN = credentials('github-token')
    SONAR_TOKEN  = credentials('sonar-token-maintainx')
    SONAR_PROJECT_KEY = 'maintainx'
  }

  triggers {
    pollSCM('H/2 * * * *')
  }

   tools {
    maven 'Maven_3'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build, Test, Package & Sonar') {
               steps {
                   withSonarQubeEnv('LocalSonar') {
                      powershell '''
                                     mvn -B -T 1C clean verify sonar:sonar `
                                         -DskipITs=true `
                                         "-Dsonar.projectKey=$env:SONAR_PROJECT_KEY" `
                                         "-Dsonar.host.url=$env:SONAR_HOST_URL" `
                                         "-Dsonar.token=$env:SONAR_TOKEN"
                                 '''
                   }
                   archiveArtifacts artifacts: 'target/*.jar'
               }
           }


    stage('UI Tests (Selenium)') {
      when {
         expression { return params.RUN_UI_TESTS }
         }
         steps {
            powershell '''
                       mvn -B -T 1C verify `
                         "-DskipUnitTests=true" `
                         "-DskipITs=false" `
                         "-Dit.test=*E2ETest" `
                         "-Dspring.profiles.active=ui-test" `
                         "-Dselenium.baseUrl=http://localhost:8080"
            '''
         }
    }


    stage('Quality Gate') {
      steps {
        timeout(time: 5, unit: 'MINUTES') {
          waitForQualityGate abortPipeline: true
        }
      }
    }
  }

  post {
    always {
      junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml,target/failsafe-reports/*.xml'

      publishHTML(target: [
        reportDir: 'target/site/jacoco',
        reportFiles: 'index.html',
        reportName: 'JaCoCo Code Coverage',
        keepAll: true,
        alwaysLinkToLastBuild: true
      ])

      archiveArtifacts artifacts: 'target/screenshots/**', fingerprint: false, allowEmptyArchive: true
      archiveArtifacts artifacts: 'target/*.jar', fingerprint: true, allowEmptyArchive: true
    }
  }
}
