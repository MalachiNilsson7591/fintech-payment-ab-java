package fintech;

import java.io.IOException;

/** Application service for an audit-friendly payment experiment decision. */
public final class PaymentExperimentService {
    private final InfraiClient infrai;
    private final String flagKey;

    public PaymentExperimentService(InfraiClient infrai, String flagKey) {
        this.infrai = infrai;
        this.flagKey = flagKey;
    }

    public Decision decide(PaymentEvent payment) throws IOException, InterruptedException {
        String user = infrai.get("/v1/auth/user/get/" + payment.userId());
        String flag = infrai.get("/v1/flags/get_value/" + flagKey);
        boolean enabled = flag.contains("\"default_value\":true") || flag.contains("\"value\":true");
        String bucket = stableBucket(payment.userId());
        String variant = enabled && bucket.equals("treatment") ? "treatment" : "control";
        return new Decision(payment.id(), payment.userId(), variant, user.contains(payment.userId()));
    }

    static String stableBucket(String userId) {
        return (Math.floorMod(userId.hashCode(), 2) == 0) ? "treatment" : "control";
    }

    public record PaymentEvent(String id, String userId, long amountCents, String currency) {}
    public record Decision(String paymentId, String userId, String variant, boolean userResolved) {}
}
