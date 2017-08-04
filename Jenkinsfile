pipeline {
    agent any
    environment {
		WAR_BUILD_DEST = "build/libs/mdl-server.war"
		WAR_DEPLOY_DEST = "$HOME/apps/tomcat8/apache-tomcat-8.5.11/webapps/"
    }
    stages {
    	stage('setup') {
			steps {
				sh 'cp $HOME/configs/config.properties ./src/main/resources/'
				sh 'cp $HOME/configs/logback-test.xml ./src/main/resources/'
				sh 'cp $HOME/configs/tomcat.xml .'
			}
		}
        stage('build') {
            steps {
				sh './gradlew assemble'
            }
        }
		stage('test') {
			steps {
				sh './gradlew test'
			}
		}
		stage('deploy') {
			steps {
				sh 'cp ${WAR_BUILD_DEST} ${WAR_DEPLOY_DEST}${BRANCH_NAME}.war'
			}
		}
    }
    post {
        always {
            junit '**/test-results/test/*.xml'
			sh './gradlew clean'
			archive '*.log'
        }
    }
}
