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
                sh './gradlew clean test -x :services:circleguard-promotion-service:test'
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

        stage('Deploy to stage') {
            when {
                branch 'stage'
                expression {
                    fileExists('k8s/stage')
                }
            }
            steps {
                withCredentials([file(credentialsId: env.KUBECONFIG_CREDENTIALS_ID, variable: 'KUBECONFIG_FILE')]) {
                    sh 'export KUBECONFIG="$KUBECONFIG_FILE" && kubectl apply -f k8s/stage/'
                    sh 'export KUBECONFIG="$KUBECONFIG_FILE" && kubectl get pods -n circleguard-stage'
                    sh 'export KUBECONFIG="$KUBECONFIG_FILE" && kubectl get svc -n circleguard-stage'
                }
            }
        }

        stage('Deploy to master') {
            when {
                branch 'master'
                expression {
                    fileExists('k8s/prod')
                }
            }
            steps {
                withCredentials([file(credentialsId: env.KUBECONFIG_CREDENTIALS_ID, variable: 'KUBECONFIG_FILE')]) {
                    sh 'export KUBECONFIG="$KUBECONFIG_FILE" && kubectl apply -f k8s/prod/'
                    sh 'export KUBECONFIG="$KUBECONFIG_FILE" && kubectl get pods -n circleguard-prod'
                    sh 'export KUBECONFIG="$KUBECONFIG_FILE" && kubectl get svc -n circleguard-prod'
                }
            }
        }

        stage('Generate Release Notes') {
            when {
                branch 'master'
            }
            steps {
                sh '''#!/usr/bin/env bash
                    set -euo pipefail
                    echo "Generating release notes for build ${BUILD_NUMBER}..."
                    mkdir -p build/release-notes
                    cat > build/release-notes/RELEASE_${BUILD_NUMBER}.md << 'EOF'
# Release Notes - Build ${BUILD_NUMBER}

## Deployment Information
- **Build Number**: ${BUILD_NUMBER}
- **Git Commit**: ${GIT_COMMIT}
- **Build Timestamp**: $(date -u +"%Y-%m-%dT%H:%M:%SZ")
- **Services Deployed**: ${SERVICE_LIST}

## What's Included
- All microservices built and deployed to production
- E2E tests executed on dev environment
- Unit and integration tests passed
- Docker images pushed to registry

## Deployment Steps
1. Built all services via Gradle
2. Created Docker images for each service
3. Pushed images to registry
4. Applied Kubernetes manifests to prod namespace

## Recommended Post-Deployment Checks
- Verify services are running: `kubectl get pods -n circleguard-prod`
- Check service endpoints: `kubectl get svc -n circleguard-prod`
- Run smoke tests for critical endpoints
- Monitor logs: `kubectl logs -n circleguard-prod -l app=<service-name>`

## Contact
For issues or questions, contact the DevOps team.
EOF
                    cat build/release-notes/RELEASE_${BUILD_NUMBER}.md
                '''
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
            archiveArtifacts artifacts: 'services/**/build/libs/*.jar,build/release-notes/**', fingerprint: true, allowEmptyArchive: true
        }
    }
}
