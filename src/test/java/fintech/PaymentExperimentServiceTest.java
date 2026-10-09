package fintech;

public final class PaymentExperimentServiceTest {
    public static void main(String[] args) {
        String a = PaymentExperimentService.stableBucket("user-42");
        String b = PaymentExperimentService.stableBucket("user-42");
        if (!a.equals(b)) throw new AssertionError("bucket changed for the same user");
        if (!a.equals("treatment") && !a.equals("control")) throw new AssertionError("unknown variant");
        System.out.println("stable bucket decision: " + a);
    }
}
