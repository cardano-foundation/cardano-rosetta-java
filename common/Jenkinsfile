def COLOR_MAP = [
    'SUCCESS': 'good',
    'FAILURE': 'danger',
]

pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                echo 'Building..'
                sh "mvn -B -DskipTests clean package"
                echo 'Build successfully!!!'
            }
        }
        stage('Test') {
            steps {
                echo 'Testing..'
                sh "mvn test"
                echo 'Test successfully!!!'
            }
        }
      
        stage('Deploy') {
            steps {
                echo 'Deploying....'
                sh "mvn deploy"
                echo 'Deployment Done!!!'
            }
        }
    }
    post {
        always {
            script {
                Author_ID = sh(script: "git show -s --pretty=%an", returnStdout: true).trim()
                Author_Name = sh(script: "git show -s --pretty=%ae", returnStdout: true).trim()
            }
            slackSend channel: "#build-group-test",
                    color: COLOR_MAP[currentBuild.currentResult],
                    message: "*${currentBuild.currentResult}:* ${env.JOB_NAME} build ${env.BUILD_NUMBER} by commit author: ${Author_ID} \n More information at: ${env.BUILD_URL}"
        }
    }
}
