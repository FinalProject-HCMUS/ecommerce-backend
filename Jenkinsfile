pipeline {
    agent { label 'master' }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Run start script') {
            steps {
                sh 'chmod +x start-docker.sh'
                sh './start-docker.sh'
            }
        }
    }
}
