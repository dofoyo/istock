rem
mvn package -Dmaven.test.skip && java -jar -Dspring.profiles.active=dev target/istock-0.0.1-SNAPSHOT.jar