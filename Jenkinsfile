pipeline {
  agent any

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

    stage('Build') {
      steps { bat 'mvn -B clean package' }
    }
  }

  post {
    always {
      junit 'target/surefire-reports/*.xml'
    }
  }
}
