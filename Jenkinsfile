node("master") {
  stage("Clone the project") {
    git branch: 'main', url: 'https://github.com/seyeadamaUASZ/auth2factor.git'
  }

  stage("Compilation") {
    bat "mvn clean install -DskipTests"
  }

}