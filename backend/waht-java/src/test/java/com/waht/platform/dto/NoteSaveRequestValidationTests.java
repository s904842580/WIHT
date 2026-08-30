package com.waht.platform.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 笔记保存参数校验测试，覆盖编辑器扩大后最重要的正文大小和关联 ID 边界。
 */
class NoteSaveRequestValidationTests {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void shouldRejectOversizedContentAndNonPositiveRelationIds() {
        NoteSaveRequest request = new NoteSaveRequest();
        request.setTitle("编辑器边界测试");
        request.setContent("x".repeat(200001));
        request.setCategoryId(0L);
        request.setTagIds(List.of(-1L));

        Set<ConstraintViolation<NoteSaveRequest>> violations = validator.validate(request);
        Set<String> paths = violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertTrue(paths.contains("content"));
        assertTrue(paths.contains("categoryId"));
        assertTrue(paths.stream().anyMatch(path -> path.startsWith("tagIds")));
    }
}