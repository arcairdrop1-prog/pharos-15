# Pharos API Paywall X402 Agent

An intelligent paywall API developed in **Java** for the Pharos Agent Arena implementing the x402 payment protocol on the Atlantic Testnet (`eip155:688689`) priced in test USDC.

## 📋 Requirements Met

1. **Atlantic Testnet & USDC Pricing (Req. 01):** Configured for Atlantic Testnet (`eip155:688689`) and priced in test USDC.
2. **Multi-Route Paywall Structure (Req. 02):** Features 1 free route (`/health`) and at least 2 paid routes (`/api/standard-data` and `/api/premium-data`) with distinct pricing.
3. **Custom Facilitator Logic (Req. 03):** Implements dedicated verification (`/verify`), settlement (`/settle`), and support configuration (`/supported`) endpoints.
4. **Complete Flow & Explorer Link (Req. 04):** Handles 402 challenges, signed payments, and responds with HTTP 200 accompanied by the `PAYMENT-RESPONSE` header linked to the explorer.
5. **Robust Rejection Handling (Req. 05):** Safely rejects malformed or underpaid transactions with HTTP 402.
6. **Secure Credential Management (Req. 06):** Loads private keys exclusively via environment variables (`WALLET_PRIVATE_KEY`), never exposing them in code.

## 🚀 How to Run

1. Certifique-se de ter o **Java JDK** instalado.
2. Compile o ficheiro:
   ```bash
   javac ApiPaywallX402.java
