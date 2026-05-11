pipeline {
    agent any

    options {
        timestamps()
        ansiColor('xterm')
        disableConcurrentBuilds()
    }

    environment {
        GRADLE_OPTS = '-Dorg.gradle.daemon=false -Dorg.gradle.parallel=true -Dorg.gradle.caching=true'
        REGISTRY = 'registry.example.com/circleguard'
        REGISTRY_CREDENTIALS_ID = 'circleguard-registry'
        KUBECONFIG_CREDENTIALS_ID = 'circleguard-kubeconfig'
        SERVICE_LIST = 'circleguard-auth-service,circleguard-identity-service,circleguard-gateway-service,circleguard-form-service,circleguard-promotion-service,circleguard-notification-service'
        IMAGE_TAG = "${BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'chmod +x ./gradlew'
            }
        }

        stage('Toolchain check') {
            steps {
                sh 'java -version'
                sh './gradlew -version'
            }
        }

        stage('Unit and integration tests') {
            steps {
                sh './gradlew clean test'
            }
        }

        stage('Build artifacts') {
            steps {
                sh './gradlew bootJar -x test'
            }
        }

        stage('Build Docker images') {
            steps {
                script {
                    def services = env.SERVICE_LIST.split(',')
                    for (String serviceName : services) {
                        sh """#!/usr/bin/env bash
                            set -euo pipefail
                            docker build \
                              -f services/${serviceName}/Dockerfile \
                              -t ${REGISTRY}/${serviceName}:${IMAGE_TAG} \
                              -t ${REGISTRY}/${serviceName}:latest \
                              services/${serviceName}
                        """
                    }
                }
            }
        }

        stage('Push Docker images') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: env.REGISTRY_CREDENTIALS_ID, usernameVariable: 'REGISTRY_USER', passwordVariable: 'REGISTRY_PASSWORD')]) {
                        sh """#!/usr/bin/env bash
                            set -euo pipefail
                            echo "$REGISTRY_PASSWORD" | docker login ${REGISTRY.split('/')[0]} -u "$REGISTRY_USER" --password-stdin
                        """

                        def services = env.SERVICE_LIST.split(',')
                        for (String serviceName : services) {
                            sh """#!/usr/bin/env bash
                                set -euo pipefail
                                docker push ${REGISTRY}/${serviceName}:${IMAGE_TAG}
                                docker push ${REGISTRY}/${serviceName}:latest
                            """
                        }
                    }
                }
            }
        }

        stage('Deploy to dev') {
            when {
                expression {
                    fileExists('k8s/dev')
                }
            }
            steps {
                withCredentials([file(credentialsId: env.KUBECONFIG_CREDENTIALS_ID, variable: 'KUBECONFIG_FILE')]) {
                    sh 'export KUBECONFIG="$KUBECONFIG_FILE" && kubectl apply -f k8s/dev/'
                    sh 'export KUBECONFIG="$KUBECONFIG_FILE" && kubectl get pods -n circleguard-dev'
                    sh 'export KUBECONFIG="$KUBECONFIG_FILE" && kubectl get svc -n circleguard-dev'
                }
            }
        }

        stage('Smoke tests') {
            when {
                expression {
                    fileExists('k8s/dev')
                }
            }
            steps {
                sh '''#!/usr/bin/env bash
                    set -euo pipefail
                    echo "Smoke tests should validate public endpoints once the dev namespace is deployed."
                    echo "Suggested checks: /actuator/health, /api/v1/auth/login, /api/v1/surveys, /api/v1/health/report"
                '''
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'services/**/build/libs/*.jar', fingerprint: true, allowEmptyArchive: true
        }
    }
}
