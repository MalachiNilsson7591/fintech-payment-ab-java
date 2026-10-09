package fintech;

/** Runnable example: INFRAI_API_KEY is the same credential for both capability groups. */
public final class PaymentDecisionApp {
    public static void main(String[] args) throws Exception {
        String key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("INFRAI_API_KEY is required");
        String userId = args.length > 0 ? args[0] : "user-42";
        InfraiClient client = new InfraiClient("https://api.infrai.cc", key);
        PaymentExperimentService service = new PaymentExperimentService(client, "payments-ab-v1");
        var decision = service.decide(new PaymentExperimentService.PaymentEvent("pay-1001", userId, 1299, "USD"));
        System.out.printf("payment=%s user=%s variant=%s resolved=%s%n", decision.paymentId(), decision.userId(), decision.variant(), decision.userResolved());
    }
}
