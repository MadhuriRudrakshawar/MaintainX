pipeline {
  agent any

  options {
    disableConcurrentBuilds()
    durabilityHint('PERFORMANCE_OPTIMIZED')
    skipDefaultCheckout(true)
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

    stage('Build, Test, Coverage & Sonar') {
      steps {
        withSonarQubeEnv('LocalSonar') {
          powershell '''
            mvn -B -T 1C clean verify sonar:sonar `
              "-Dsonar.projectKey=$env:SONAR_PROJECT_KEY" `
              "-Dsonar.token=$env:SONAR_TOKEN"
          '''
        }
      }
    }

    stage('Quality Gate') {
      steps {
        timeout(time: 15, unit: 'MINUTES') {
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
    }
  }
}