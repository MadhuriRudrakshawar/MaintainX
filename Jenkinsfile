pipeline {
  agent any

  environment {
    GITHUB_TOKEN = credentials('github-token')
    SONAR_TOKEN = credentials('sonar-token')
    SONAR_PROJECT_KEY = 'maintainx'
    SONAR_HOST_URL = 'http://localhost:9000'
  }

  triggers {
    pollSCM('H/2 * * * *')
  }

  tools {
    maven 'Maven_3'
  }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Build, Test & Coverage') {
                steps {
                    powershell 'mvn clean verify'
                }
            }
    }


       stage('SonarQube Analysis') {
                steps {
                    withSonarQubeEnv('LocalSonar') {
                        powershell '''
                          mvn -B sonar:sonar "-Dsonar.projectKey=$env:SONAR_PROJECT_KEY" "-Dsonar.host.url=$env:SONAR_HOST_URL" "-Dsonar.token=$env:SONAR_TOKEN"
                        '''
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
