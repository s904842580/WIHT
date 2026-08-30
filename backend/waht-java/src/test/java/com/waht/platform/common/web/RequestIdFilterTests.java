package com.waht.platform.common.web;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * RequestIdFilter 测试，确认安全编号可以透传，非法编号会被替换。
 */
class RequestIdFilterTests {

    private final RequestIdFilter requestIdFilter = new RequestIdFilter();

    @Test
    void shouldReuseSafeRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestIdFilter.REQUEST_ID_HEADER, "waht-test-001");
        MockHttpServletResponse response = new MockHttpServletResponse();

        requestIdFilter.doFilter(request, response, (currentRequest, currentResponse) -> {
        });

        assertEquals("waht-test-001", response.getHeader(RequestIdFilter.REQUEST_ID_HEADER));
    }

    @Test
    void shouldReplaceUnsafeRequestId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestIdFilter.REQUEST_ID_HEADER, "unsafe\nvalue");
        MockHttpServletResponse response = new MockHttpServletResponse();

        requestIdFilter.doFilter(request, response, (currentRequest, currentResponse) -> {
        });

        String actualRequestId = response.getHeader(RequestIdFilter.REQUEST_ID_HEADER);
        assertNotNull(actualRequestId);
        assertNotEquals("unsafe\nvalue", actualRequestId);
    }
}
