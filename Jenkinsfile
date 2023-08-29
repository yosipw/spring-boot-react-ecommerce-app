pipeline {
    agent any
    tools { nodejs "nodejs" }


    stages {
        stage('Hello 1') {
            steps {
                git branch: 'master',
                    url: 'https://github.com/yosipw/amazon-ecs-nodejs-microservices.git'
            }
        }
        stage('Hello 3') {
            steps {
                withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'AWS_ACCESS_KEY_ID', credentialsId: '54bdbf63-cdb8-475f-bafc-ebdb6ad2ff5b', secretKeyVariable: 'AWS_SECRET_ACCESS_KEY']]) {
                    dir('3-microservices') {
                        script{
                            sh """
                            ./deploy.sh ${region} ${stackname}
                            """
                        }
                    }
                }
            }
        }
    }
}