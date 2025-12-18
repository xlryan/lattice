package com.lattice.core.infrastructure.tools;

import com.lattice.agent.finance.model.FireflyExpenseCommand;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

/**
 * Registers Spring AI tool functions backed by infrastructure clients.
 */
@Configuration
public class ToolFunctionConfiguration {

    @Bean
    @Description("Scan receipt image bytes and return structured amount/currency info")
    public Function<PythonWorkerClient.ImagePayload, PythonWorkerClient.ReceiptData> scanReceiptFunction(
            PythonWorkerClient client) {
        return client::scanReceipt;
    }

    @Bean
    @Description("Parse resume text into STAR-based insights")
    public Function<PythonWorkerClient.ResumeDocument, PythonWorkerClient.ResumeInsights> resumeParserFunction(
            PythonWorkerClient client) {
        return client::parseResume;
    }

    @Bean
    @Description("Create an expense transaction in Firefly III")
    public Function<FireflyExpenseCommand, FireflyClient.FireflyTransactionResult> fireflyExpenseFunction(
            FireflyClient client) {
        return client::createExpense;
    }
}
