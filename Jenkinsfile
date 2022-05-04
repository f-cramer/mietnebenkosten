pipeline {
	agent any
	tools {
		jdk 'jdk17'
	}

	stages {
		stage('Build') {
			steps {
				sh './gradlew assemble --refresh-dependencies --continue'
			}
		}
		stage('Tests') {
			steps {
				sh './gradlew check --continue'
			}
			post {
				always {
					junit allowEmptyResults: true, testResults: '**/build/test-results/test/*.xml'
					recordIssues enabledForFailure: true, tools: [spotBugs(pattern: 'build/reports/spotbugs/*.xml', reportEncoding: 'UTF-8', useRankAsPriority: true)]
				}
			}
		}
	}

	post {
		changed {
			postChanged()
		}
		failure {
			postFailure()
		}
	}
}
