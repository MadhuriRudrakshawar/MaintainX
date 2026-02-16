pipeline {
  agent any

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

    stage('Build, Test & Coverage') {
      steps {
        powershell 'mvn -B clean verify'
      }
    }

    stage('SonarQube Analysis') {
      steps {
        withSonarQubeEnv('LocalSonar') {
          powershell '''
            mvn -B sonar:sonar `
              "-Dsonar.projectKey=$env:SONAR_PROJECT_KEY" `
              "-Dsonar.token=$env:SONAR_TOKEN"
          '''
        }
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
      junit 'target/surefire-reports/*.xml'

    }
  }
}
