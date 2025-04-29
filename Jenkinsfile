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

        stage('Load .env file') {
            steps {
                withCredentials([file(credentialsId: 'environment-file', variable: 'ENV_FILE')]) {
                    sh '''
                        # Export variables from .env file
                        set -a  # Automatically export all variables
                        . $ENV_FILE
                        set +a
                        # Example: Print a variable
                        echo "MAIL_USERNAME=$MAIL_USERNAME"
                    '''
                }
            }
        }

        stage('Run Start Script') {
            steps {
                sh '''
                    chmod +x start-docker.sh
                    ./start-docker.sh
                '''
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
