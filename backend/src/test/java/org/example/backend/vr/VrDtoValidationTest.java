package org.example.backend.vr;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.example.backend.dto.vr.VrCategoryReqDto;
import org.example.backend.dto.vr.VrSceneReqDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class VrDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("测试 VR分类入参校验：名称或编码为空时触发拦截")
    void testVrCategoryValidation() {
        VrCategoryReqDto invalidDto = VrCategoryReqDto.builder()
                .name("")
                .code("")
                .build();

        Set<ConstraintViolation<VrCategoryReqDto>> violations = validator.validate(invalidDto);
        assertFalse(violations.isEmpty(), "空分类名称与编码应当被校验拦截");
        assertEquals(2, violations.size());

        VrCategoryReqDto validDto = VrCategoryReqDto.builder()
                .name("顺峰山公园-西区")
                .code("west_park")
                .sort(1)
                .build();
        violations = validator.validate(validDto);
        assertTrue(violations.isEmpty(), "合法参数应当通过校验");
    }

    @Test
    @DisplayName("测试 VR场景入参校验：百分比坐标超范围应被拦截")
    void testVrSceneCoordinateValidation() {
        VrSceneReqDto invalidDto = VrSceneReqDto.builder()
                .categoryId(1001L)
                .name("测试场景")
                .panoramaUrl("https://vr.gentingincubus.com/vr/panoramas/test.jpg")
                .topPercent(new BigDecimal("105.00")) // 超过 100%
                .leftPercent(new BigDecimal("-2.00")) // 小于 0%
                .initialDeg(200) // 超过 180 度
                .build();

        Set<ConstraintViolation<VrSceneReqDto>> violations = validator.validate(invalidDto);
        assertFalse(violations.isEmpty(), "超出边界的坐标与角度应当被校验拦截");
        assertEquals(3, violations.size());

        VrSceneReqDto validDto = VrSceneReqDto.builder()
                .categoryId(1001L)
                .name("伏波桥")
                .panoramaUrl("https://vr.gentingincubus.com/vr/panoramas/fuboqiao.jpg")
                .previewUrl("https://vr.gentingincubus.com/vr/previews/fuboqiao.jpg")
                .topPercent(new BigDecimal("35.50"))
                .leftPercent(new BigDecimal("50.20"))
                .initialDeg(-32)
                .sort(1)
                .status(1)
                .build();

        violations = validator.validate(validDto);
        assertTrue(violations.isEmpty(), "合法的场景参数应完全通过校验");
    }
}

