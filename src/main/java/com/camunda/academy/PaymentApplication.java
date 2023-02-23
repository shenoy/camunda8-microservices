package com.camunda.academy;
import com.camunda.academy.handler.CreditCardServiceHandler;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobWorker;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProvider;
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class PaymentApplication {
    private static final String ZEEBE_ADDRESS="f86587d7-7ee7-439c-a608-16e64f0e53f9.dsm-1.zeebe.camunda.io:443";
    private static final String ZEEBE_CLIENT_ID="unZjObUPrBzerJlugphXpqZKOb08FhZh";
    private static final String ZEEBE_CLIENT_SECRET="ZP.dYw3K1wfs3VlEoCLQDS9R83t3IRhKcn.hi2sAfVP1snn5GNhvd8~7-iXD0wkT";
    private static final String ZEEBE_AUTHORIZATION_SERVER_URL="https://login.cloud.camunda.io/oauth/token";
    private static final String ZEEBE_TOKEN_AUDIENCE="zeebe.camunda.io";


    public static void main(String[] args) {

        final OAuthCredentialsProvider credentialsProvider =
            new OAuthCredentialsProviderBuilder()
                .authorizationServerUrl(ZEEBE_AUTHORIZATION_SERVER_URL)
                .audience(ZEEBE_TOKEN_AUDIENCE)
                .clientId(ZEEBE_CLIENT_ID)
                .clientSecret(ZEEBE_CLIENT_SECRET)
                .build();

        try (final ZeebeClient client =
                 ZeebeClient.newClientBuilder()
                     .gatewayAddress(ZEEBE_ADDRESS)
                     .credentialsProvider(credentialsProvider)
                     .build()) {

            final Map<String, Object> variables = new HashMap<String, Object>();
            variables.put("reference", "C8_12345");
            variables.put("amount", Double.valueOf(100.00));
            variables.put("cardNumber", "1234567812345678");
            variables.put("cardExpiry", "12/2023");
            variables.put("cardCVC", "123");

            client.newCreateInstanceCommand()
                .bpmnProcessId("paymentProcess")
                .latestVersion()
                .variables(variables)
                .send()
                .join();

        final JobWorker creditCardWorker =
            client.newWorker()
                .jobType("chargeCreditCard")
                .handler(new CreditCardServiceHandler())
                .timeout(Duration.ofSeconds(10).toMillis())
                .open();
            Thread.sleep(10000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
