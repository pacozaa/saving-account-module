For the Deposit service, I use business decomposition pattern where I breakdown the responsibility into smaller, focused services. This help with independent scalability and the atomic database service can be used by multiple services.

---

Achieving RPO = 0 (Zero Data Loss) - use multi AZs synchronous replication
Achieving RTO ≤ 4 Hours (Recovery Time) - First, for Stateless services - this can be deploy in secondary region or zone. Second, use Database Failover for Stateful services. 

---

Selected Tech Stack:
I choose Spring Boot microservices architecture for building this system because of the challenging of the mission to finish within a short period of time. The Spring Boot give me the tools I need to finish quickly.
- Programming Language: Java with Spring Boot
- Service Discovery: Spring Cloud Netflix Eureka
- API Gateway: Spring Cloud Gateway
- Database: H2 In-Memory Database