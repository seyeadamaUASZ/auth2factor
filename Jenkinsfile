pipeline {
	agent any

	environment {
		mavenHome = tool 'jenkins-maven'
	}

	tools {
		jdk 'java-17'
	}

	stages {

		stage('Build'){
			steps {
				bat "mvn clean install -DskipTests"
			}
		}

		stage('Test'){
			steps{
				bat "mvn test"
			}
		}

		stage('sonar quality'){
		steps {
		   withSonarQubeEnv(installationName:'sonarqube'){
                   bat 'mvn clean install -DskipTests org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.0.2155:sonar -Dsonar.java.binaries=target/classes'
           }
		}
		}

		stage('Deploy jar to nexus repo') {
			steps {
			    bat "mvn jar:jar deploy:deploy"
			}
		}
	}
}