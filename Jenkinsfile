pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = "your-docker-registry.com"
        APP_NAME_GATEWAY = "gateway-service"
        APP_NAME_ACCOUNT = "account-service"
        IMAGE_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh 'mvn clean install -Dnet.bytebuddy.experimental=true'
            }
        }

        stage('Docker Build & Push') {
            steps {
                script {
                    // Build and Push Gateway Service
                    sh "docker build -t ${DOCKER_REGISTRY}/${APP_NAME_GATEWAY}:${IMAGE_TAG} ./gateway-service"
                    sh "docker push ${DOCKER_REGISTRY}/${APP_NAME_GATEWAY}:${IMAGE_TAG}"

                    // Build and Push Account Service
                    sh "docker build -t ${DOCKER_REGISTRY}/${APP_NAME_ACCOUNT}:${IMAGE_TAG} ./account-service"
                    sh "docker push ${DOCKER_REGISTRY}/${APP_NAME_ACCOUNT}:${IMAGE_TAG}"
                }
            }
        }

        stage('Deploy to QA') {
            when { branch 'develop' }
            steps {
                script {
                    sh "kubectl apply -f infrastructure/kubernetes/qa/deployment.yaml --namespace=ledger-qa"
                    sh "kubectl set image deployment/gateway-qa gateway=${DOCKER_REGISTRY}/${APP_NAME_GATEWAY}:${IMAGE_TAG} --namespace=ledger-qa"
                    sh "kubectl set image deployment/account-qa account=${DOCKER_REGISTRY}/${APP_NAME_ACCOUNT}:${IMAGE_TAG} --namespace=ledger-qa"
                }
            }
        }

        stage('Deploy to UAT') {
            when { branch 'release/*' }
            steps {
                script {
                    sh "kubectl apply -f infrastructure/kubernetes/uat/deployment.yaml --namespace=ledger-uat"
                    sh "kubectl set image deployment/gateway-uat gateway=${DOCKER_REGISTRY}/${APP_NAME_GATEWAY}:${IMAGE_TAG} --namespace=ledger-uat"
                    sh "kubectl set image deployment/account-uat account=${DOCKER_REGISTRY}/${APP_NAME_ACCOUNT}:${IMAGE_TAG} --namespace=ledger-uat"
                }
            }
        }

        stage('Deploy to Production') {
            when { branch 'main' }
            options {
                timeout(time: 1, unit: 'HOURS')
            }
            steps {
                input message: "Approve deployment to Production?"
                script {
                    sh "kubectl apply -f infrastructure/kubernetes/prod/deployment.yaml --namespace=ledger-prod"
                    sh "kubectl set image deployment/gateway-prod gateway=${DOCKER_REGISTRY}/${APP_NAME_GATEWAY}:${IMAGE_TAG} --namespace=ledger-prod"
                    sh "kubectl set image deployment/account-prod account=${DOCKER_REGISTRY}/${APP_NAME_ACCOUNT}:${IMAGE_TAG} --namespace=ledger-prod"

                    // Verify rollout
                    sh "kubectl rollout status deployment/gateway-prod --namespace=ledger-prod"
                }
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo "Pipeline completed successfully!"
        }
        failure {
            echo "Pipeline failed. Please check logs."
        }
    }
}
