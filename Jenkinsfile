pipeline {
    agent { label 'agent' }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Check User') {
            steps {
                sh 'whoami'
            }
        }

        stage('Run Start Script') {
            steps {
                withCredentials([file(credentialsId: 'environment-file', variable: 'ENV_FILE')]) {
                    sh '''
                        #!/bin/bash
                        set -a
                        source $ENV_FILE
                        set +a
                        chmod +x start-docker.sh
                        ./start-docker.sh
                    '''
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline finished. Checking final status...'
        }
        success {
            echo 'Pipeline completed successfully! 🎉'
        }
        failure {
            echo 'Pipeline failed. Investigate the errors! ❌'
        }
        aborted {
            echo 'Pipeline was aborted. 🛑'
        }
    }
}
