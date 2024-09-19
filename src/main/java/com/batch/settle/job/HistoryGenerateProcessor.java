package com.batch.settle.job;

import com.batch.settle.domain.ApiOrderHistory;
import com.batch.settle.domain.ApiProduct;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.LongStream;

@Component
public class HistoryGenerateProcessor implements ItemProcessor<Boolean, ApiOrderHistory> {
    private final List<Long> userIds = LongStream.range(0, 19).boxed().toList();
    private final List<ApiProduct> products = Arrays.stream(ApiProduct.values()).toList();
    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    @Override
    public ApiOrderHistory process(Boolean item) throws Exception {
        var randomCustomerId = userIds.get(random.nextInt(userIds.size()));
        var randomApiProduct = products.get(random.nextInt(products.size()));
        var randomState = random.nextInt(5) % 5 == 1 ? ApiOrderHistory.State.FAIL : ApiOrderHistory.State.SUCCESS;

        return new ApiOrderHistory(
            UUID.randomUUID().toString(),
            randomCustomerId,
            randomApiProduct.url(),
            randomApiProduct.fee(),
            randomState,
            ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        );
    }
}
