For the **Deposit** feature, I leverage the **Service Orchestration (Composition)** pattern, where the Deposit Service acts as a coordinator that invokes both the Transaction and Account services to complete a single request.

The rationale behind this selection is to enforce **Separation of Concerns**, keeping the workflow logic of a "deposit" distinct from the atomic data operations of logging history and updating balances. A primary benefit is **reusability**, as the underlying Account and Transaction services remain generic and can be consumed by other features (like Transfers) without code duplication. This pattern also supports **independent scalability**, allowing the Deposit Service to scale up during peak teller hours without requiring the entire backend to resize. However, the major trade-off is the complexity of managing **distributed transactions**, often requiring mechanisms like Sagas to ensure data consistency across the Accounts and Transactions databases if a partial failure occurs.

---

Achieving RPO = 0 (Zero Data Loss) - use multi AZs synchronous replication
Achieving RTO ≤ 4 Hours (Recovery Time) - First, for Stateless services - this can be deploy in secondary region or zone. Second, use Database Failover for Stateful services. 