pipeline {
    agent any

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    parameters {
        booleanParam(
            name: 'DEPLOY_TO_K8S',
            defaultValue: false,
            description: 'Deploy to local Kubernetes after successful build'
        )
    }

    environment {
        IMAGE_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
            }
        }

        stage('Backend Tests') {
            steps {
                echo 'Running Spring Boot backend tests...'
                sh './ci/backend-test.sh'
            }
        }

        stage('Frontend Build') {
            steps {
                echo 'Building React frontend...'
                sh './ci/frontend-build.sh'
            }
        }

        stage('ML Service Validation') {
            steps {
                echo 'Validating Python ML service...'
                sh './ci/ml-validate.sh'
            }
        }

        stage('Docker Build') {
            steps {
                echo "Building Docker images with tag ${IMAGE_TAG}..."
                sh "./ci/docker-build.sh ${IMAGE_TAG}"
            }
        }

        stage('Docker Image Summary') {
            steps {
                sh '''
                    docker images | grep enterprise-inventory || true
                '''
            }
        }

        stage('Kubernetes Deploy') {
            when {
                expression {
                    return params.DEPLOY_TO_K8S
                }
            }
            steps {
                echo 'Deploying to Kubernetes...'
                sh './ci/k8s-deploy.sh'
            }
        }
    }

    post {
        success {
            echo 'CI/CD pipeline completed successfully.'
        }

        failure {
            echo 'CI/CD pipeline failed. Check logs above.'
        }

        always {
            echo 'Pipeline finished.'
        }
    }
}
