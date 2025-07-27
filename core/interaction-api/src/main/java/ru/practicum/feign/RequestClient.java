package ru.practicum.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "request-service", path = "/events")
public interface RequestClient {
}
