#!/usr/bin/env groovy

node {
    stage('checkout') {
        checkout scm
    }

    stage('check java') {
        sh "java -version"
    }

    stage('clean') {
        sh "chmod +x mvnw"
        sh "./mvnw clean"
    }

    stage('backend tests') {
       try {
           sh "./mvnw test"
       } catch(err) {
           throw err
       } finally {
           junit '**/target/surefire-reports/TEST-*.xml'
       }
    }

    stage('build & move') {
          sh "./mvnw deploy -Pprod -DskipTests"
          archiveArtifacts artifacts: '**/target/*.war', fingerprint: true
    }

    stage('quality analysis') {
       withSonarQubeEnv('Sonar') {
           sh "./mvnw sonar:sonar"
       }
    }
/*
    def dockerImage
    stage('build docker') {
        sh "cp -R src/main/docker target/"
        sh "cp target/*.war target/docker/"
        dockerImage = docker.build('athma/pharmacy', 'target/docker')
    }

    stage('publish docker') {
        docker.withRegistry('https://localhost:5000') {
            dockerImage.push 'latest'
        }
    }
*/    
}
