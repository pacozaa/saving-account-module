- This is Spring Boot microservices project.
- This repository is a Saving Account Module. To build this within short period of time, you must prioritize **velocity and simplicity** over enterprise-grade perfection. You need to cut corners on complexity (like distributed tracing or complex Sagas) while maintaining a clean, working architecture.
- When need to create new service, Use Spring Boot CLI to create a new project with the necessary dependencies for a RESTful web service. For example:
```
spring init --dependencies=web,data-jpa,actuator my-microservice

# With more options
spring init --dependencies=web,cloud-eureka,cloud-config-client \
  --build=gradle \
  --java-version=17 \
  --packaging=jar \
  my-microservice
```
- Execute small steps at a time - for example, first create the basic project structure, stop and ask user if everything looks good, then add REST endpoints, stop and ask again, then implement business logic and stop and ask again, and so on.
- Do not do everything in one go.
- Always follow best practices for Spring Boot microservices, such as using configuration files for environment-specific
- One module at a time.
- Use in-memory H2 database for development and testing.
- Use Spring Cloud Netflix Eureka for service discovery.
- Use Spring Cloud Gateway as the API gateway.
- Use Spring Cloud OpenFeign for inter-service communication.
- No need for prefix `api/` in the REST endpoints/services's controller because api-gateway handles routing.