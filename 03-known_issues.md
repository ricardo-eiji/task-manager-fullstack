About this file
- unresolved problems, why they're deferred, and how to reproduce/revisit them. Short, scannable, not a full debugging transcript.

- Professionally, the acceptable version is: note it (e.g. a KNOWN_ISSUES.md line or ticket), understand roughly why (env/config drift, not a code bug), and move on if it doesn't block delivery

Problem with the cloned repo
- `mvn test` / `mvn spring-boot:run` locally are broken due to some local Postgres auth quirk (likely WSL-specific, unrelated to your code).
    - Since Docker is your primary way of running/verifying this project (and it's confirmed 100% working), this local issue doesn't block anything important. 
    - Suggest: drop it for now, move on. We can revisit only if it starts blocking actual work