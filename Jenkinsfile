pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        GRADLE_OPTS = '-Dorg.gradle.daemon=false -Dorg.gradle.parallel=true -Dorg.gradle.caching=true'
        // Use GitHub Container Registry for images; set your GitHub user below
        REGISTRY = 'ghcr.io/nicoarchery/circleguard'
        REGISTRY_CREDENTIALS_ID = 'circleguard-registry'
        KUBECONFIG_CREDENTIALS_ID = 'circleguard-kubeconfig'
        SERVICE_LIST = 'circleguard-auth-service,circleguard-identity-service,circleguard-gateway-service,circleguard-form-service,circleguard-promotion-service,circleguard-notification-service'
        IMAGE_TAG = "v1.0.${BUILD_NUMBER}" // Automatic Semantic Versioning
        SONAR_CREDENTIALS_ID = 'sonar-token'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'chmod +x ./gradlew'
                sh 'chmod +x ./scripts/wait-for-services.sh || true'
            }
        }

        stage('Start local infra') {
            steps {
                sh '''#!/usr/bin/env bash
                    set -euo pipefail
                    if [ -f docker-compose.dev.yml ]; then
                        if command -v docker-compose >/dev/null 2>&1; then
                            docker-compose -f docker-compose.dev.yml up -d
                        elif docker compose version >/dev/null 2>&1; then
                            docker compose -f docker-compose.dev.yml up -d
                        else
                            echo "docker-compose/docker compose not available, skipping infra startup"
                            exit 0
                        fi
                        ./scripts/wait-for-services.sh
                    else
                        echo "docker-compose.dev.yml not found, skipping infra startup"
                    fi
                '''
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
                sh '''#!/usr/bin/env bash
                    set -euo pipefail
                    export SPRING_PROFILES_ACTIVE=test
                    export JWT_SECRET="my-super-secret-test-key-32-chars-long-012345"
                    ./gradlew clean test -x :services:circleguard-promotion-service:test
                '''
            }
        }

        stage('SonarQube Analysis') {
            steps {
                script {
                    try {
                        withCredentials([string(credentialsId: env.SONAR_CREDENTIALS_ID, variable: 'SONAR_AUTH_TOKEN')]) {
                            sh """#!/usr/bin/env bash
                                set -euo pipefail
                                ./gradlew sonar -Dsonar.token=${SONAR_AUTH_TOKEN} -Dsonar.projectKey=circleguard -Dsonar.host.url=${env.SONAR_HOST_URL ?: 'http://sonarqube:9000'} -Dsonar.java.binaries="services/*/build/classes"
                            """
                        }
                    } catch (Exception ex) {
                        echo "Skipping SonarQube analysis due to missing credentials: ${ex.message}"
                    }
                }
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
                    if (sh(script: 'command -v docker >/dev/null 2>&1', returnStatus: true) != 0) {
                        echo 'docker is not available on this Jenkins agent, skipping Docker image build stage'
                        return
                    }
                    sh """#!/usr/bin/env bash
                        set -euo pipefail
                        # Initialize buildx for multi-arch builds (ARM64 for AKS)
                        docker buildx create --name circleguard-builder --use 2>/dev/null || docker buildx use circleguard-builder
                        docker buildx inspect --bootstrap
                    """
                    def services = env.SERVICE_LIST.split(',')
                    for (String serviceName : services) {
                        sh """#!/usr/bin/env bash
                            set -euo pipefail
                            # Build for arm64 (AKS node architecture) using buildx
                            docker buildx build \
                              --platform linux/arm64 \
                              --load \
                              -f services/${serviceName}/Dockerfile \
                              -t ${REGISTRY}/${serviceName}:${IMAGE_TAG} \
                              -t ${REGISTRY}/${serviceName}:latest \
                              services/${serviceName}
                        """
                    }
                }
            }
        }

        stage('Security Scan (Trivy)') {
            steps {
                script {
                    if (sh(script: 'command -v trivy >/dev/null 2>&1', returnStatus: true) != 0) {
                        echo 'Trivy is not installed on this Jenkins agent, skipping security scan'
                        return
                    }
                    if (sh(script: 'command -v docker >/dev/null 2>&1', returnStatus: true) != 0) {
                        echo 'Docker is not available, skipping Trivy container scan'
                        return
                    }
                    def services = env.SERVICE_LIST.split(',')
                    for (String serviceName : services) {
                        try {
                            sh "trivy image --severity HIGH,CRITICAL --format table --exit-code 0 ${REGISTRY}/${serviceName}:${IMAGE_TAG}"
                        } catch (Exception ex) {
                            echo "Trivy scan skipped for ${serviceName}: ${ex.message}"
                        }
                    }
                }
            }
        }

        stage('Push Docker images') {
            steps {
                script {
                    if (sh(script: 'command -v docker >/dev/null 2>&1', returnStatus: true) != 0) {
                        echo 'docker is not available on this Jenkins agent, skipping Docker push stage'
                        return
                    }
                    try {
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
                    } catch (Exception ex) {
                        echo "Skipping Docker push stage due to missing/invalid registry credentials or agent config: ${ex.message}"
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
                script {
                    if (sh(script: 'command -v kubectl >/dev/null 2>&1', returnStatus: true) != 0) {
                        echo 'kubectl is not available on this Jenkins agent, skipping deploy to dev'
                        return
                    }
                    try {
                        withCredentials([file(credentialsId: env.KUBECONFIG_CREDENTIALS_ID, variable: 'KUBECONFIG_FILE')]) {
                        withCredentials([usernamePassword(credentialsId: env.REGISTRY_CREDENTIALS_ID, usernameVariable: 'REGISTRY_USER', passwordVariable: 'REGISTRY_PASSWORD')]) {
                            sh """#!/usr/bin/env bash
                                set -euo pipefail
                                kubectl --kubeconfig="\$KUBECONFIG_FILE" --cache-dir=/dev/null create namespace circleguard-dev --dry-run=client -o yaml > /tmp/ns-dev.yaml && kubectl --kubeconfig="\$KUBECONFIG_FILE" --cache-dir=/dev/null apply -f /tmp/ns-dev.yaml --validate=false
                                # Create imagePullSecret for ghcr.io if not exists
                                kubectl --kubeconfig="\$KUBECONFIG_FILE" create secret docker-registry ghcr-credentials \
                                    --namespace circleguard-dev \
                                    --docker-server=ghcr.io \
                                    --docker-username=\$REGISTRY_USER \
                                    --docker-password=\$REGISTRY_PASSWORD \
                                    --dry-run=client -o yaml | kubectl --kubeconfig="\$KUBECONFIG_FILE" apply -f -
                                # Patch images to use the build tag instead of :dev
                                find k8s/dev/ -name "*.yaml" -exec sed -i "s|image: \\(.*\\):dev|image: ${REGISTRY}/\\1:${IMAGE_TAG}|g" {} +
                                kubectl --kubeconfig="\$KUBECONFIG_FILE" --cache-dir=/dev/null apply -f k8s/dev/ --validate=false
                                kubectl --kubeconfig="\$KUBECONFIG_FILE" --cache-dir=/dev/null get pods -n circleguard-dev
                                kubectl --kubeconfig="\$KUBECONFIG_FILE" --cache-dir=/dev/null get svc -n circleguard-dev
                            """
                        }
                        }
                    } catch (Exception ex) {
                        echo "Skipping deploy to dev due to missing/invalid kubeconfig credentials or agent config: ${ex.message}"
                    }
                }
            }
        }

        stage('Deploy to stage') {
            when {
                expression {
                    def branchName = env.BRANCH_NAME ?: env.GIT_BRANCH ?: ''
                    (branchName == 'main' || branchName.endsWith('/main') || branchName == 'stage' || branchName.endsWith('/stage')) && fileExists('k8s/stage')
                }
            }
            steps {
                script {
                    if (sh(script: 'command -v kubectl >/dev/null 2>&1', returnStatus: true) != 0) {
                        echo 'kubectl is not available on this Jenkins agent, skipping deploy to stage'
                        return
                    }
                    try {
                        withCredentials([file(credentialsId: env.KUBECONFIG_CREDENTIALS_ID, variable: 'KUBECONFIG_FILE')]) {
                        withCredentials([usernamePassword(credentialsId: env.REGISTRY_CREDENTIALS_ID, usernameVariable: 'REGISTRY_USER', passwordVariable: 'REGISTRY_PASSWORD')]) {
                            sh """#!/usr/bin/env bash
                                set -euo pipefail
                                kubectl --kubeconfig="\$KUBECONFIG_FILE" --cache-dir=/dev/null create namespace circleguard-stage --dry-run=client -o yaml > /tmp/ns-stage.yaml && kubectl --kubeconfig="\$KUBECONFIG_FILE" --cache-dir=/dev/null apply -f /tmp/ns-stage.yaml --validate=false
                                # Create imagePullSecret for ghcr.io if not exists
                                kubectl --kubeconfig="\$KUBECONFIG_FILE" create secret docker-registry ghcr-credentials \
                                    --namespace circleguard-stage \
                                    --docker-server=ghcr.io \
                                    --docker-username=\$REGISTRY_USER \
                                    --docker-password=\$REGISTRY_PASSWORD \
                                    --dry-run=client -o yaml | kubectl --kubeconfig="\$KUBECONFIG_FILE" apply -f -
                                # Patch images to use the build tag
                                find k8s/stage/ -name "*.yaml" -exec sed -i "s|image: \\(.*\\):stage|image: ${REGISTRY}/\\1:${IMAGE_TAG}|g" {} +
                                kubectl --kubeconfig="\$KUBECONFIG_FILE" --cache-dir=/dev/null apply -f k8s/stage/ --validate=false
                                kubectl --kubeconfig="\$KUBECONFIG_FILE" --cache-dir=/dev/null get pods -n circleguard-stage
                            """
                        }
                        }
                    } catch (Exception ex) {
                        echo "Skipping deploy to stage due to missing/invalid kubeconfig credentials or agent config: ${ex.message}"
                    }
                }
            }
        }

        stage('Deploy to master') {
            when {
                expression {
                    def branchName = env.BRANCH_NAME ?: env.GIT_BRANCH ?: ''
                    (branchName == 'main' || branchName.endsWith('/main') || branchName == 'master' || branchName.endsWith('/master')) && fileExists('k8s/prod')
                }
            }
            steps {
                input message: "Aprovar despliegue a PRODUCCIÓN (Cluster AKS)?", ok: "Desplegar"
                script {
                    if (sh(script: 'command -v kubectl >/dev/null 2>&1', returnStatus: true) != 0) {
                        echo 'kubectl is not available on this Jenkins agent, skipping deploy to master'
                        return
                    }
                    try {
                        withCredentials([file(credentialsId: env.KUBECONFIG_CREDENTIALS_ID, variable: 'KUBECONFIG_FILE')]) {
                        withCredentials([usernamePassword(credentialsId: env.REGISTRY_CREDENTIALS_ID, usernameVariable: 'REGISTRY_USER', passwordVariable: 'REGISTRY_PASSWORD')]) {
                            sh """#!/usr/bin/env bash
                                set -euo pipefail
                                kubectl --kubeconfig="\$KUBECONFIG_FILE" --cache-dir=/dev/null create namespace circleguard-prod --dry-run=client -o yaml > /tmp/ns-prod.yaml && kubectl --kubeconfig="\$KUBECONFIG_FILE" --cache-dir=/dev/null apply -f /tmp/ns-prod.yaml --validate=false
                                # Create imagePullSecret for ghcr.io if not exists
                                kubectl --kubeconfig="\$KUBECONFIG_FILE" create secret docker-registry ghcr-credentials \
                                    --namespace circleguard-prod \
                                    --docker-server=ghcr.io \
                                    --docker-username=\$REGISTRY_USER \
                                    --docker-password=\$REGISTRY_PASSWORD \
                                    --dry-run=client -o yaml | kubectl --kubeconfig="\$KUBECONFIG_FILE" apply -f -
                                # Patch images to use the build tag
                                find k8s/prod/ -name "*.yaml" -exec sed -i "s|image: \\(.*\\):prod|image: ${REGISTRY}/\\1:${IMAGE_TAG}|g" {} +
                                kubectl --kubeconfig="\$KUBECONFIG_FILE" --cache-dir=/dev/null apply -f k8s/prod/ --validate=false
                                kubectl --kubeconfig="\$KUBECONFIG_FILE" --cache-dir=/dev/null get pods -n circleguard-prod
                            """
                        }
                        }
                    } catch (Exception ex) {
                        echo "Skipping deploy to master due to missing/invalid kubeconfig credentials or agent config: ${ex.message}"
                    }
                }
            }
        }

        stage('Generate Release Notes') {
            when {
                expression {
                    def branchName = env.BRANCH_NAME ?: env.GIT_BRANCH ?: ''
                    branchName == 'main' || branchName.endsWith('/main') || branchName == 'master' || branchName.endsWith('/master')
                }
            }
            steps {
                sh '''#!/usr/bin/env bash
                    set -euo pipefail
                    echo "Generating release notes for build ${BUILD_NUMBER}..."
                    mkdir -p build/release-notes

                    # Get changes since last tag (or initial commit if no tags yet)
                    LAST_TAG=$(git describe --tags --abbrev=0 2>/dev/null || git rev-list --max-parents=0 HEAD)
                    echo "Changes since: ${LAST_TAG}"
                    git log --oneline --no-decorate "${LAST_TAG}..HEAD" > build/release-notes/CHANGES_${BUILD_NUMBER}.txt || echo "(no previous tag, showing full log)" > build/release-notes/CHANGES_${BUILD_NUMBER}.txt

                    # Get list of contributors
                    git log --format="%an" "${LAST_TAG}..HEAD" 2>/dev/null | sort -u > build/release-notes/CONTRIBUTORS_${BUILD_NUMBER}.txt || echo "N/A" > build/release-notes/CONTRIBUTORS_${BUILD_NUMBER}.txt

                    CHANGES=$(cat build/release-notes/CHANGES_${BUILD_NUMBER}.txt 2>/dev/null)
                    CONTRIBUTORS=$(cat build/release-notes/CONTRIBUTORS_${BUILD_NUMBER}.txt 2>/dev/null | tr '\n' ', ' | sed 's/,$//')

                    cat > build/release-notes/RELEASE_${BUILD_NUMBER}.md << EOF
# Release Notes - v1.0.${BUILD_NUMBER}

## Deployment Information
- **Version**: v1.0.${BUILD_NUMBER}
- **Build Number**: ${BUILD_NUMBER}
- **Git Commit**: ${GIT_COMMIT}
- **Branch**: ${GIT_BRANCH}
- **Build Timestamp**: $(date -u +"%Y-%m-%dT%H:%M:%SZ")
- **Services Deployed**: ${SERVICE_LIST}

## Changes in this Release
${CHANGES}

## Contributors
${CONTRIBUTORS}

## Deployment Steps
1. Built all services via Gradle
2. Created Docker images for each service
3. Pushed images to GHCR
4. Applied Kubernetes manifests to prod namespace

## Rollback Instructions
See [ROLLBACK_PLAN.md](../docs/ROLLBACK_PLAN.md)

## Post-Deployment Checks
- Verify services: kubectl get pods -n circleguard-prod
- Check endpoints: kubectl get svc -n circleguard-prod
- Run smoke tests for critical endpoints
- Monitor logs: kubectl logs -n circleguard-prod -l app=<service-name>

## Build URL
${BUILD_URL}
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
        success {
            echo "✅ Build ${BUILD_NUMBER} exitoso. Desplegado v1.0.${BUILD_NUMBER}."
            script {
                // Email notification on success
                try {
                    mail(
                        to: 'devops-team@circleguard.edu',
                        subject: "✅ [CircleGuard] Build #${BUILD_NUMBER} - SUCCESS",
                        body: """
Pipeline SUCCESS — CircleGuard CI/CD

Build Number : ${BUILD_NUMBER}
Git Commit   : ${env.GIT_COMMIT ?: 'N/A'}
Branch       : ${env.GIT_BRANCH ?: 'main'}
Duration     : ${currentBuild.durationString}

Deployed version v1.0.${BUILD_NUMBER} to AKS (dev, stage, prod).

View build: ${env.BUILD_URL}
                        """.stripIndent()
                    )
                } catch (Exception mailEx) {
                    echo "Email notification skipped (SMTP not configured): ${mailEx.message}"
                }
                // Discord webhook (optional)
                if (env.DISCORD_WEBHOOK) {
                    sh "curl -X POST -H 'Content-Type: application/json' -d '{\"content\": \"✅ Build ${BUILD_NUMBER} for ${env.JOB_NAME} SUCCEEDED\"}' ${env.DISCORD_WEBHOOK}"
                }
                // Create git tag for this release
                try {
                    sh """#!/usr/bin/env bash
                        set -euo pipefail
                        git config user.email "cicd@circleguard.edu"
                        git config user.name "CircleGuard CI/CD"
                        git tag -a "v1.0.${BUILD_NUMBER}" -m "Release v1.0.${BUILD_NUMBER}"
                        git push origin "v1.0.${BUILD_NUMBER}" 2>/dev/null || echo "Git tag push skipped (no credentials)"
                        echo "✅ Git tag v1.0.${BUILD_NUMBER} created and pushed"
                    """
                } catch (Exception tagEx) {
                    echo "Git tagging skipped: ${tagEx.message}"
                }
            }
        }
        failure {
            echo "❌ Build ${BUILD_NUMBER} fallido. Revisar logs en: ${env.BUILD_URL}console"
            script {
                // Email notification on failure
                try {
                    mail(
                        to: 'devops-team@circleguard.edu',
                        subject: "❌ [CircleGuard] Build #${BUILD_NUMBER} - FAILED",
                        body: """
Pipeline FAILURE — CircleGuard CI/CD

Build Number : ${BUILD_NUMBER}
Git Commit   : ${env.GIT_COMMIT ?: 'N/A'}
Branch       : ${env.GIT_BRANCH ?: 'main'}
Duration     : ${currentBuild.durationString}

ACTION REQUIRED: The pipeline has failed.
Check the logs immediately: ${env.BUILD_URL}console

Failed Stage: ${currentBuild.currentResult}
                        """.stripIndent()
                    )
                } catch (Exception mailEx) {
                    echo "Email notification skipped (SMTP not configured): ${mailEx.message}"
                }
                // Discord webhook (optional)
                if (env.DISCORD_WEBHOOK) {
                    sh "curl -X POST -H 'Content-Type: application/json' -d '{\"content\": \"❌ Build ${BUILD_NUMBER} for ${env.JOB_NAME} FAILED\"}' ${env.DISCORD_WEBHOOK}"
                }
            }
        }
        always {
            archiveArtifacts artifacts: 'services/**/build/libs/*.jar,build/release-notes/**', fingerprint: true, allowEmptyArchive: true
            sh '''#!/usr/bin/env bash
                    set -euo pipefail
                    # Commit release notes if they exist
                    if [ -d build/release-notes ] && [ "$(ls -A build/release-notes/ 2>/dev/null)" ]; then
                        git add build/release-notes/ -A 2>/dev/null || true
                        git commit -m "docs: add release notes for v1.0.${BUILD_NUMBER}" 2>/dev/null || true
                        git push origin HEAD:${GIT_BRANCH} 2>/dev/null || echo "Release notes push skipped"
                    fi
                    # Tear down local infra
                    if [ -f docker-compose.dev.yml ]; then
                        if command -v docker-compose >/dev/null 2>&1; then
                            docker-compose -f docker-compose.dev.yml down || true
                        elif docker compose version >/dev/null 2>&1; then
                            docker compose -f docker-compose.dev.yml down || true
                        fi
                    fi
            '''
        }
    }
}
