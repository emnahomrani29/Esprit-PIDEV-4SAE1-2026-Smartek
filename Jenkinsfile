pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }
    
    environment {
        // SonarQube
        SONAR_HOST = 'http://192.168.56.10:9000'
        SONAR_LOGIN = credentials('sonarqube-token')
        
        // Docker Registry
        DOCKER_REGISTRY = 'localhost:5000'
        
        // Kubernetes
        K8S_NAMESPACE = 'smartek'
        
        // Git
        GIT_REPO = 'https://github.com/emnahomrani29/Esprit-PI-4SAE1-2026-Smartek.git'
        GIT_BRANCH = 'main'
    }
    
    stages {
        stage('📥 Checkout Code') {
            steps {
                echo '📥 Récupération du code depuis Git...'
                git branch: "${GIT_BRANCH}", url: "${GIT_REPO}"
            }
        }
        
        stage('🏗️ Build Backend Services') {
            parallel {
                stage('Auth Service') {
                    steps {
                        dir('Backend/auth-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Course Service') {
                    steps {
                        dir('Backend/course-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Exam Service') {
                    steps {
                        dir('Backend/exam-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Planning Service') {
                    steps {
                        dir('Backend/planning-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Training Service') {
                    steps {
                        dir('Backend/training-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Event Service') {
                    steps {
                        dir('Backend/event-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
                stage('Certification Badge Service') {
                    steps {
                        dir('Backend/certification-badge-service') {
                            sh 'mvn clean package -DskipTests'
                        }
                    }
                }
            }
        }
        
        stage('🧪 Tests Unitaires') {
            parallel {
                stage('Backend Tests') {
                    steps {
                        script {
                            def services = [
                                'auth-service',
                                'course-service',
                                'exam-service',
                                'planning-service',
                                'training-service',
                                'event-service',
                                'certification-badge-service'
                            ]
                            
                            services.each { service ->
                                dir("Backend/${service}") {
                                    sh 'mvn test'
                                }
                            }
                        }
                    }
                }
                stage('Frontend Tests') {
                    steps {
                        dir('Frontend/angular-app') {
                            sh 'npm install'
                            sh 'npm test -- --watch=false --browsers=ChromeHeadless'
                        }
                    }
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('🔍 SonarQube Analysis') {
            steps {
                echo '🔍 Analyse de la qualité du code avec SonarQube...'
                script {
                    def services = [
                        'auth-service',
                        'course-service',
                        'exam-service',
                        'planning-service',
                        'training-service',
                        'event-service',
                        'certification-badge-service'
                    ]
                    
                    services.each { service ->
                        dir("Backend/${service}") {
                            sh """
                                mvn sonar:sonar \
                                    -Dsonar.host.url=${SONAR_HOST} \
                                    -Dsonar.login=${SONAR_LOGIN} \
                                    -Dsonar.projectKey=smartek-${service} \
                                    -Dsonar.projectName='Smartek ${service}'
                            """
                        }
                    }
                }
            }
        }
        
        stage('🚦 Quality Gate') {
            steps {
                echo '🚦 Vérification du Quality Gate SonarQube...'
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        
        stage('🐳 Build Docker Images') {
            parallel {
                stage('Auth Service Image') {
                    steps {
                        script {
                            sh """
                                docker build -t ${DOCKER_REGISTRY}/auth-service:${BUILD_NUMBER} \
                                    -t ${DOCKER_REGISTRY}/auth-service:latest \
                                    Backend/auth-service
                                docker push ${DOCKER_REGISTRY}/auth-service:${BUILD_NUMBER}
                                docker push ${DOCKER_REGISTRY}/auth-service:latest
                            """
                        }
                    }
                }
                stage('Course Service Image') {
                    steps {
                        script {
                            sh """
                                docker build -t ${DOCKER_REGISTRY}/course-service:${BUILD_NUMBER} \
                                    -t ${DOCKER_REGISTRY}/course-service:latest \
                                    Backend/course-service
                                docker push ${DOCKER_REGISTRY}/course-service:${BUILD_NUMBER}
                                docker push ${DOCKER_REGISTRY}/course-service:latest
                            """
                        }
                    }
                }
                stage('Exam Service Image') {
                    steps {
                        script {
                            sh """
                                docker build -t ${DOCKER_REGISTRY}/exam-service:${BUILD_NUMBER} \
                                    -t ${DOCKER_REGISTRY}/exam-service:latest \
                                    Backend/exam-service
                                docker push ${DOCKER_REGISTRY}/exam-service:${BUILD_NUMBER}
                                docker push ${DOCKER_REGISTRY}/exam-service:latest
                            """
                        }
                    }
                }
                stage('Planning Service Image') {
                    steps {
                        script {
                            sh """
                                docker build -t ${DOCKER_REGISTRY}/planning-service:${BUILD_NUMBER} \
                                    -t ${DOCKER_REGISTRY}/planning-service:latest \
                                    Backend/planning-service
                                docker push ${DOCKER_REGISTRY}/planning-service:${BUILD_NUMBER}
                                docker push ${DOCKER_REGISTRY}/planning-service:latest
                            """
                        }
                    }
                }
                stage('Frontend Image') {
                    steps {
                        script {
                            sh """
                                docker build -t ${DOCKER_REGISTRY}/frontend:${BUILD_NUMBER} \
                                    -t ${DOCKER_REGISTRY}/frontend:latest \
                                    Frontend/angular-app
                                docker push ${DOCKER_REGISTRY}/frontend:${BUILD_NUMBER}
                                docker push ${DOCKER_REGISTRY}/frontend:latest
                            """
                        }
                    }
                }
            }
        }
        
        stage('☸️ Deploy to Kubernetes') {
            steps {
                echo '☸️ Déploiement sur Kubernetes...'
                script {
                    sh """
                        # Créer namespace si n'existe pas
                        kubectl create namespace ${K8S_NAMESPACE} || true
                        
                        # Appliquer les manifests
                        kubectl apply -f k8s/namespace.yml
                        kubectl apply -f k8s/configmap.yml
                        kubectl apply -f k8s/secret.yml
                        kubectl apply -f k8s/infrastructure/
                        
                        # Mettre à jour les images des services
                        kubectl set image deployment/auth-service \
                            auth-service=${DOCKER_REGISTRY}/auth-service:${BUILD_NUMBER} \
                            -n ${K8S_NAMESPACE}
                        
                        kubectl set image deployment/course-service \
                            course-service=${DOCKER_REGISTRY}/course-service:${BUILD_NUMBER} \
                            -n ${K8S_NAMESPACE}
                        
                        kubectl set image deployment/exam-service \
                            exam-service=${DOCKER_REGISTRY}/exam-service:${BUILD_NUMBER} \
                            -n ${K8S_NAMESPACE}
                        
                        kubectl set image deployment/planning-service \
                            planning-service=${DOCKER_REGISTRY}/planning-service:${BUILD_NUMBER} \
                            -n ${K8S_NAMESPACE}
                        
                        kubectl set image deployment/frontend \
                            frontend=${DOCKER_REGISTRY}/frontend:${BUILD_NUMBER} \
                            -n ${K8S_NAMESPACE}
                        
                        # Attendre le rollout
                        kubectl rollout status deployment/auth-service -n ${K8S_NAMESPACE} --timeout=5m
                        kubectl rollout status deployment/course-service -n ${K8S_NAMESPACE} --timeout=5m
                        kubectl rollout status deployment/exam-service -n ${K8S_NAMESPACE} --timeout=5m
                        kubectl rollout status deployment/planning-service -n ${K8S_NAMESPACE} --timeout=5m
                        kubectl rollout status deployment/frontend -n ${K8S_NAMESPACE} --timeout=5m
                    """
                }
            }
        }
        
        stage('✅ Health Check') {
            steps {
                echo '✅ Vérification de la santé des services...'
                script {
                    sh """
                        # Vérifier que les pods sont running
                        kubectl get pods -n ${K8S_NAMESPACE}
                        
                        # Attendre que tous les pods soient Ready
                        kubectl wait --for=condition=ready pod \
                            -l app=auth-service \
                            -n ${K8S_NAMESPACE} \
                            --timeout=300s
                        
                        # Tester les endpoints
                        sleep 30
                        curl -f http://192.168.56.10:30080/actuator/health || exit 1
                    """
                }
            }
        }
    }
    
    post {
        always {
            echo '🧹 Nettoyage...'
            cleanWs()
        }
        success {
            echo '✅ Pipeline terminé avec succès !'
            echo "🎉 Build #${BUILD_NUMBER} déployé sur Kubernetes"
            echo "🌐 Application accessible sur : http://192.168.56.10:30080"
        }
        failure {
            echo '❌ Pipeline échoué !'
            echo '🔄 Rollback automatique en cours...'
            script {
                sh """
                    kubectl rollout undo deployment/auth-service -n ${K8S_NAMESPACE} || true
                    kubectl rollout undo deployment/course-service -n ${K8S_NAMESPACE} || true
                    kubectl rollout undo deployment/exam-service -n ${K8S_NAMESPACE} || true
                    kubectl rollout undo deployment/planning-service -n ${K8S_NAMESPACE} || true
                    kubectl rollout undo deployment/frontend -n ${K8S_NAMESPACE} || true
                """
            }
        }
    }
}
