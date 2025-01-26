<h1>🛠️ Prerequisites</h1>
Before running the project, ensure you have the following installed:

<li>Java 17</li>
<li>Maven</li>
<li>MySQL Database</li>

<h1>📦 Project Setup and Run Instructions</h1>

<h3>Configure Application Properties</h3>

### Update the src/main/resources/application.properties file with your database credentials:
```->
spring.datasource.url=jdbc:mysql://<DB_HOST>:<DB_PORT>/<DB_NAME> <br>
spring.datasource.username=<DB_USERNAME> <br>
spring.datasource.password=<DB_PASSWORD> <br>
```

### Code Build and Run
```->
mvn clean install
```
```->
java -jar target/UserAccessManager-0.0.1-SNAPSHOT.jar
```
