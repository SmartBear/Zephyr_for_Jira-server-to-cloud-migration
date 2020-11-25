# Zephyr_for_Jira-server-to-cloud-migration
#### Zephyr for Jira Server to Cloud Migration

###### Please read the below instructions to setup the configuration
1. Run ‘mvn clean install’ command to build the package.
2. Run ‘mvn spring-boot:run’ command to run the application. 
This will launch the spring boot server at default port 8080.
3. The default properties can be updated in application.properties file.

###### Prerequisite for the application.
`1) Server Base Url
2) Server Admin Credentials
3) Cloud Zephyr Admin User Credentials.
{AccountId, AccessKey, SecretKey}`

###### **To Trigger the migration**

Launch the application at http://{localhost:port}/beginMigration   

Provide the project id & begin the process.


