# har-to-jersey-rest-and-desc-with-swagger
This is a POC, which uses a har definition to create jersey resources at deployment. Swagger is used to check, which resource are available.
## How to use
1. Grab a HAR (HTTP Archive) definition and store at your file system. Currently only GETs and POSTs are used. Don't duplicate URLs, there is no check, which would avoid the registration of two resources for the same URL.
2. java -Dharpath="pathttoyourhar from 1." -jar payara-microprofile.jar --deploy har-to-jersey-rest-and-desc-with-swagger.war
3. Open the URL http://localhost:8080/har-to-jersey-rest-and-desc-with-swagger/swagger.json to see available resources
