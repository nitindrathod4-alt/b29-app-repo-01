pipeline {
    agent any

    stages {

        stage('PULL STAGE') {
            steps {
                git 'https://github.com/nitindrathod4-alt/b29-app-repo-01.git'
            }
        }

        stage('PULL STAGE 2') {
            steps {
                git 'https://github.com/nitindrathod4-alt/b29-app-repo-01.git'
            }
        }

        stage('FRONTEND-DOCKER-BUILD') {
            steps {
                sh '''
                cd frontend
                docker build -t nitinrathod07/hard-frontend:latest .
                '''
            }
        }

        stage('BACKEND-DOCKER-BUILD') {
            steps {
                sh '''
                cd backend
                docker build -t nitinrathod07/hard-backend:latest .
                '''
            }
        }

        stage('DOCKER-PUSH') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub',
                        usernameVariable: 'DOCKER_USER',
                        passwordVariable: 'DOCKER_PASS'
                    )
                ]) {
                    sh '''
                    echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin

                    docker push nitinrathod07/hard-frontend:latest
                    docker push nitinrathod07/hard-backend:latest

                    docker logout
                    '''
                }
            }
        }

        stage('DOCKER-CLEAN') {
            steps {
                sh '''
                docker rmi -f nitinrathod07/hard-frontend:latest || true
                docker rmi -f nitinrathod07/hard-backend:latest || true
                '''
            }
        }

        stage('DEPLOY') {
            steps {
                sh '''
                set -e

                aws sts get-caller-identity

                aws eks list-clusters --region ap-south-1

                aws eks update-kubeconfig \
                    --region ap-south-1 \
                    --name my-cluster

                kubectl get nodes

                kubectl apply -f simple-deploy/

                kubectl get pods -A
                '''
            }
        }
    }
}
