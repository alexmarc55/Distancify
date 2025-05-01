# 🚨 Emergency Dispatch System

> A full-stack emergency dispatch coordination system developed during the **Distancify 2025 Hackathon**.

## 🧠 Inspiration

In emergency response, time is critical. We set out to build a modern, automated dispatch system that optimizes resource allocation while still allowing for human intervention in high-risk cases. The project simulates real-world scenarios where intelligent systems assist dispatchers to make faster, smarter decisions.

## 💡 What It Does

This full-stack application acts as an intelligent emergency dispatch system with the following core functionalities:

- 📡 **REST API Communication**: The backend receives emergency case data from an **emulator** that simulates real-time scenarios such as number of available vehicles in different cities, new incidents, etc.
- 🧠 **Greedy Algorithm**: A greedy strategy is used on the server to determine the best allocation of available units to reported emergencies.
- 💾 **SQL Persistence**: All events, allocations, and outcomes are stored in a **SQL database** for full traceability and analysis.
- ⚠️ **Critical Case Escalation**: More dangerous or ambiguous cases are sent to the **ReactJS frontend**, where a human operator can make the final decision.
- 🔄 **Bi-directional Flow**: The decision made on the frontend is then sent back to the server and recorded as a resolved case.

## 🛠️ Built With

- **Java** – Backend server logic and API layer
- **ReactJS** – Frontend interface for human operator decisions
- **SQL** – Database for persistent storage
- **REST APIs** – Communication between the emulator, server, and frontend

## 🖥️ System Architecture

1. **Emulator** simulates emergency scenarios and sends requests to the backend via REST.
2. **Backend (Java)** handles case assignment using greedy logic and stores everything in the database.
3. **Frontend (React)** displays critical cases to human operators for manual resolution.
4. Final decisions are sent back to the backend for database update and closure.
