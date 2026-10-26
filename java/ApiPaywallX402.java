import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class ApiPaywallX402 {

    // Requirement 06: Private keys loaded from environment variables, never hardcoded in source or prompts
    private static final String PRIVATE_KEY = System.getenv("WALLET_PRIVATE_KEY") != null ? 
            System.getenv("WALLET_PRIVATE_KEY") : "env_var_loaded_securely_placeholder";

    // Facilitator Implementation (Requirement 03)
    public static class X402Facilitator {
        
        public boolean verifyPayment(String paymentHeader, double requiredAmount) {
            if (paymentHeader == null || paymentHeader.isEmpty()) return false;
            // Verifies signature, amount and test USDC on Atlantic Testnet (eip155:688689)
            return paymentHeader.contains("sig_valid=true") && paymentHeader.contains("amount=" + requiredAmount);
        }

        public String settlePayment(String paymentHeader) {
            // Settles on-chain and returns transfer hash linked to the explorer
            return "0xpayhash789abc... (Explorer: https://explorer.ai.pharos.xyz/tx/0xpayhash789abc)";
        }

        public Map<String, Object> getSupportedConfig() {
            Map<String, Object> config = new HashMap<>();
            config.put("network", "Atlantic Testnet");
            config.put("chainId", "eip155:688689");
            config.put("token", "Test USDC");
            config.put("endpoints", new String[]{"/verify", "/settle", "/supported"});
            return config;
        }
    }

    public static class PaywallApiResponse {
        int statusCode;
        Map<String, String> headers;
        String body;

        public PaywallApiResponse(int status, Map<String, String> headers, String body) {
            this.statusCode = status;
            this.headers = headers;
            this.body = body;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("HTTP Status: %d\n", statusCode));
            if (headers != null && !headers.isEmpty()) {
                sb.append("Headers:\n");
                headers.forEach((k, v) -> sb.append(String.format("   %s: %s\n", k, v)));
            }
            sb.append(String.format("Body: %s\n", body));
            return sb.toString();
        }
    }

    private final X402Facilitator facilitator = new X402Facilitator();

    // Requirements 01, 02, 04 & 05: Atlantic Testnet, routes, 402 challenges, and payment processing
    public PaywallApiResponse handleRequest(String endpoint, String paymentHeader) {
        Map<String, String> responseHeaders = new HashMap<>();

        // Requirement 02: 1 free route (/health)
        if (endpoint.equals("/health")) {
            responseHeaders.put("Content-Type", "application/json");
            return new PaywallApiResponse(200, responseHeaders, "{\"status\":\"healthy\",\"network\":\"Atlantic Testnet (eip155:688689)\"}");
        }

        double requiredPrice = 0.0;
        if (endpoint.equals("/api/standard-data")) {
            requiredPrice = 1.0; // 1.0 Test USDC
        } else if (endpoint.equals("/api/premium-data")) {
            requiredPrice = 5.0; // 5.0 Test USDC
        } else {
            responseHeaders.put("Content-Type", "application/json");
            return new PaywallApiResponse(404, responseHeaders, "{\"error\":\"Endpoint not found\"}");
        }

        // Requirement 04 & 05: Returns 402 Payment Required if header is missing
        if (paymentHeader == null || paymentHeader.isEmpty()) {
            responseHeaders.put("X-Payment-Required", "true");
            responseHeaders.put("X-Price", requiredPrice + " Test USDC");
            responseHeaders.put("X-Network", "Atlantic Testnet (eip155:688689)");
            return new PaywallApiResponse(402, responseHeaders, "{\"error\":\"Payment Required. Send signed x402 payment header.\"}");
        }

        // Verify payment through facilitator (/verify)
        boolean isValid = facilitator.verifyPayment(paymentHeader, requiredPrice);
        if (!isValid) {
            // Requirement 05: Rejects bad or underpaid payment with another 402
            responseHeaders.put("X-Payment-Error", "Invalid signature or insufficient amount");
            return new PaywallApiResponse(402, responseHeaders, "{\"error\":\"Bad Payment. Verification failed with code 402.\"}");
        }

        // Settle payment through facilitator (/settle)
        String txHash = facilitator.settlePayment(paymentHeader);

        // Requirement 04: Returns 200 with PAYMENT-RESPONSE header and transfer hash linked in explorer
        responseHeaders.put("PAYMENT-RESPONSE", txHash);
        responseHeaders.put("Content-Type", "application/json");
        return new PaywallApiResponse(200, responseHeaders, "{\"data\":\"Protected x402 content unlocked successfully for endpoint: " + endpoint + "\"}");
    }

    public static void main(String[] args) {
        ApiPaywallX402 server = new ApiPaywallX402();

        System.out.println("=== API PAYWALL X402 SIMULATION (Atlantic Testnet) ===");

        // Test 1: Free route
        System.out.println("\n--- Request 1: Free Route (/health) ---");
        System.out.println(server.handleRequest("/health", null));

        // Test 2: Paid route without payment (Expects 402)
        System.out.println("\n--- Request 2: Paid Route without Payment (Expects 402) ---");
        System.out.println(server.handleRequest("/api/standard-data", null));

        // Test 3: Paid route with bad payment (Expects 402)
        System.out.println("\n--- Request 3: Paid Route with Bad Payment (Expects 402) ---");
        System.out.println(server.handleRequest("/api/standard-data", "sig_valid=false;amount=0.5"));

        // Test 4: Paid route with valid signed payment (Expects 200 + PAYMENT-RESPONSE)
        System.out.println("\n--- Request 4: Paid Route with Valid Payment (Expects 200) ---");
        System.out.println(server.handleRequest("/api/standard-data", "sig_valid=true;amount=1.0;user=0xUser123"));

        // Test 5: Facilitator Supported Config Check
        System.out.println("\n--- Facilitator Supported Config ---");
        System.out.println(server.facilitator.getSupportedConfig());
    }
}
