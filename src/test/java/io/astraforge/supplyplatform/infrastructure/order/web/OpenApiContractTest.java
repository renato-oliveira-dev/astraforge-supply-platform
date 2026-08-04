package io.astraforge.supplyplatform.infrastructure.order.web;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiContractTest {

    private static final String CONTRACT_PATH =
            "static/openapi/order-api.yaml";

    @Test
    void testContractShouldDocumentEveryOrderEndpoint() throws IOException {
        String contract = loadContract();
        List<String> expectedPaths = List.of(
                "/api/v1/orders:",
                "/api/v1/orders/{orderId}:",
                "/api/v1/orders/{orderId}/items:",
                "/api/v1/orders/{orderId}/items/{orderItemId}:",
                "/api/v1/orders/{orderId}/submission:",
                "/api/v1/orders/{orderId}/approval:",
                "/api/v1/orders/{orderId}/approval-decisions/approval:",
                "/api/v1/orders/{orderId}/approval-decisions/rejection:",
                "/api/v1/orders/{orderId}/inventory-reservations:",
                "/api/v1/orders/{orderId}/fulfillment/completion:");

        assertThat(contract)
                .as("OpenAPI version")
                .contains("openapi: 3.1.0");
        assertThat(expectedPaths)
                .as("documented order API paths")
                .allSatisfy(path -> assertThat(contract)
                        .as("documented path " + path)
                        .contains("  " + path));
    }

    @Test
    void testContractShouldDefineUniqueOperationIdentifiers()
            throws IOException {
        String contract = loadContract();
        List<String> operationIds = contract.lines()
                .map(String::trim)
                .filter(line -> line.startsWith("operationId:"))
                .map(line -> line.substring("operationId:".length()).trim())
                .toList();

        assertThat(operationIds)
                .as("OpenAPI operation identifiers")
                .isNotEmpty()
                .doesNotHaveDuplicates();
    }

    @Test
    void testContractShouldDocumentStandardizedErrors()
            throws IOException {
        assertThat(loadContract())
                .as("standardized API error contract")
                .contains(
                        "ApiErrorResponse:",
                        "correlationId:",
                        "BadRequest:",
                        "NotFound:",
                        "UnprocessableContent:");
    }

    private static String loadContract() throws IOException {
        ClassLoader classLoader =
                OpenApiContractTest.class.getClassLoader();
        try (InputStream inputStream =
                     classLoader.getResourceAsStream(CONTRACT_PATH)) {
            assertThat(inputStream)
                    .as("OpenAPI classpath resource")
                    .isNotNull();
            return new String(
                    inputStream.readAllBytes(),
                    StandardCharsets.UTF_8);
        }
    }
}
