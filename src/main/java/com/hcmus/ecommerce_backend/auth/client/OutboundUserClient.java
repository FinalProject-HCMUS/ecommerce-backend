package com.hcmus.ecommerce_backend.auth.client;

import com.hcmus.ecommerce_backend.auth.model.dto.response.OutboundUserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client interface for interacting with the external user information provider.
 * This interface defines methods for making HTTP requests to the user information provider's API.
 */
@FeignClient(name = "outbound-user-client", url = "https://www.googleapis.com")
public interface OutboundUserClient {

    /**
     * Retrieves user information from the external provider.
     * This method sends a GET request to the user information endpoint.
     *
     * @param alt the format of the response (e.g., json)
     * @param accessToken the access token used for authentication
     * @return the response object containing the user information
     */
    @GetMapping(value = "/oauth2/v1/userinfo")
    OutboundUserResponse getUserInfo(@RequestParam("alt") String alt,
                                     @RequestParam("access_token") String accessToken);
}
