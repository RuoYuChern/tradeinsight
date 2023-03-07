package com.tao.trade.infra.http;

import feign.RetryableException;
import feign.Retryer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeignRetry implements Retryer {
    @Override
    public void continueOrPropagate(RetryableException e) {
        log.info("RetryableException:{}", e.getMessage());
        throw e;
    }

    @Override
    public Retryer clone() {
        return new Retryer.Default();
    }
}
