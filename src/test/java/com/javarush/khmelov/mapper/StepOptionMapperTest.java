package com.javarush.khmelov.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.javarush.khmelov.dto.StepOptionDto;
import com.javarush.khmelov.entity.Step;
import com.javarush.khmelov.entity.StepOption;
import org.junit.jupiter.api.Test;

public class StepOptionMapperTest {

    @Test
    void toDto_returnsNull_whenOptionIsNull() {
        StepOptionDto dto = StepOptionMapper.toDto(null);
        assertNull(dto, "Mapping null StepOption should return null");
    }

    @Test
    void toDto_mapsFieldsCorrectly_whenNextStepIsNotNull() {
        Step nextStep = new Step();
        nextStep.setId(42);

        StepOption option = new StepOption();
        option.setId(1);
        option.setOptionText("Choose me");
        option.setNextStep(nextStep);

        StepOptionDto dto = StepOptionMapper.toDto(option);

        assertNotNull(dto);
        assertEquals(option.getId(), dto.getId());
        assertEquals(option.getOptionText(), dto.getOptionText());
        assertEquals(nextStep.getId(), dto.getNextStepId());
    }

    @Test
    void toDto_setsNextStepIdNull_whenNextStepIsNull() {
        StepOption option = new StepOption();
        option.setId(2);
        option.setOptionText("No next step");
        option.setNextStep(null);

        StepOptionDto dto = StepOptionMapper.toDto(option);

        assertNotNull(dto);
        assertEquals(option.getId(), dto.getId());
        assertEquals(option.getOptionText(), dto.getOptionText());
        assertNull(dto.getNextStepId(), "NextStepId should be null if nextStep is null");
    }
}