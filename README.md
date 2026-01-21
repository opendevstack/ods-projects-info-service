# About

This repository contains the source code for the Projects Info Service backend service.

Also, under the "scripts" directory, the required scripts and OpenAPI specifications required for generating the required API REST clients and server.

## 1. Create a Spring Boot launch configuration

- Go to the main() method for the application: `org.opendevstack.projects_info_service.ProjectsInfoServiceApplication.main()`
- Click on its "Play" icon > Modify Run Configuration...
- A dialog will appear, click on "Ok" button in order to save the new launch configuration

## 2. Configure the Spring Boot launch configuration
This step requires setting the required env variables and the active profile to "local".

### 2.1. Customize env variables for local development
As a preliminary step to get the required env variables with the correct values for local development, 
we will copy-and-modify an env vars template file.

Do the following:
- Copy the template file: 
    - Original: `src/main/resources/application-local.env.template` 
    - Copy: `src/main/resources/application-local.env`
- Customize the copied file with the required values for local development

**NOTES** 
- Files matching the `src/main/resources/*.env` pattern are git-ignored, so they won't be accidentally committed or pushed to the repository.
- Encrypted values are currently **not** supported in the `application-local.env` file.

### 2.2. Modify the Spring Boot launch configuration

Do the following:
- Open the Run/Debug Configuration dialog
- Set the "Active profiles" to just "local" value
- Press Alt+E to enable the "Environment variables" textbox and click on the "Browse for .env files and scripts" icon:
- Browse and select the `src/main/resources/application-local.env` you just created
- At the end of the process, you should have a configuration similar to the following:

## 3. Customize application-local.yml file

The `application.yml` file takes some property values from the env vars, and `application-local.yml` config file 
inherits those properties and values from the `application.yml` file. 

This means that no further customization is required regarding those inherited properties and values.

Set other properties in the `application-local.yml` file as needed for local development, e.g. debug level, enabled actuators, local server port, etc.

## 4. Edit secrets in the local Vault server
To do this you will need both the tailor installation and the oc executable.
- For the tailor installation: https://github.com/opendevstack/tailor and follow the README
- For the oc console, download it from the openshift site, click on the question mark next to the user profile link: https://console-openshift-console.apps.us-test.ocp.aws.boehringer.com/command-line-tools
- Once everything is set, you can run the following command:
 `tailor secrets edit devstack-dev.env.enc --private-key="${ROUTE_TO_folderXYZ}/tailor-private.key" --public-key-dir="${ROUTE_TO_folderXYZ}"`
 notes:
    - press a to enter in insert mode
    - do your updates
    - press esc to exit insert mode
    - type :wq to save and exit

## 5. Import certificates to JVM Truststore
To do this you will need all the certificates inside the docker/cert folder and a terminal of your choice (tested using Git Bash):
- Verify the Java truststore path
    - `$JAVA_HOME/lib/security/cacerts`
- Go to the folder where the certificates are, and run the following script:
    - `
        for cert in *.crt; do
            alias=$(basename "$cert" .crt)
            echo "Importing $cert using alias $alias..."
            keytool -import -trustcacerts -keystore "$JAVA_HOME/lib/security/cacerts" \
                -storepass changeit -noprompt -alias "$alias" -file "$cert"
        done`
- You can check the certificates where correctly imported using the following command:
    - `keytool -list -keystore "$JAVA_HOME/lib/security/cacerts" -storepass changeit`
	

# Quality
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=opendevstack_ods-projects-info-service&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=opendevstack_ods-projects-info-service)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=opendevstack_ods-projects-info-service&metric=coverage)](https://sonarcloud.io/summary/new_code?id=opendevstack_ods-projects-info-service)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=opendevstack_ods-projects-info-service&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=opendevstack_ods-projects-info-service)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=opendevstack_ods-projects-info-service&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=opendevstack_ods-projects-info-service)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=opendevstack_ods-projects-info-service&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=opendevstack_ods-projects-info-service)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=opendevstack_ods-projects-info-service&metric=bugs)](https://sonarcloud.io/summary/new_code?id=opendevstack_ods-projects-info-service)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=opendevstack_ods-projects-info-service&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=opendevstack_ods-projects-info-service)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=opendevstack_ods-projects-info-service&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=opendevstack_ods-projects-info-service)
