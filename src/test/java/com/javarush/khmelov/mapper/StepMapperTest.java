package com.javarush.khmelov.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.javarush.khmelov.dto.StepDto;
import com.javarush.khmelov.dto.StepOptionDto;
import com.javarush.khmelov.entity.Step;
import com.javarush.khmelov.entity.StepOption;
import org.junit.jupiter.api.Test;

import java.util.List;

public class StepMapperTest {

    @Test
    void toDto_returnsNull_whenStepIsNull() {
        StepDto dto = StepMapper.toDto((Step) null);
        assertNull(dto, "DTO should be null when Step is null");
    }

    @Test
    void toDto_returnsNull_whenStepOptionIsNull() {
        StepOptionDto dto = StepMapper.toDto((StepOption) null);
        assertNull(dto, "DTO should be null when StepOption is null");
    }

    @Test
    void toDto_mapsStepFieldsCorrectly() {
        Step step = new Step();
        step.setId(1);
        step.setText("Step text");
        step.setFinish(true);
        step.setWin(false);

        StepDto dto = StepMapper.toDto(step);

        assertNotNull(dto);
        assertEquals(step.getId(), dto.getId());
        assertEquals(step.getText(), dto.getText());
        assertEquals(step.getFinish(), dto.getFinish());
        assertEquals(step.getWin(), dto.getWin());
        assertNotNull(dto.getOptions(), "Options should not be null");
        assertTrue(dto.getOptions().isEmpty(), "Options should be empty if step.options is empty");
    }

    @Test
    void toDto_mapsStepWithOptionsCorrectly() {
        Step nextStep = new Step();
        nextStep.setId(2);

        StepOption option1 = new StepOption();
        option1.setId(10);
        option1.setOptionText("Option 1");
        option1.setNextStep(nextStep);

        Step step = new Step();
        step.setId(1);
        step.setText("Step with options");
        step.setOptions(List.of(option1));

        StepDto dto = StepMapper.toDto(step);

        assertNotNull(dto);
        assertEquals(1, dto.getOptions().size());

        StepOptionDto optionDto = dto.getOptions().get(0);
        assertEquals(option1.getId(), optionDto.getId());
        assertEquals(option1.getOptionText(), optionDto.getOptionText());
        assertEquals(nextStep.getId(), optionDto.getNextStepId());
    }

    @Test
    void toDto_handlesNullNextStepInOption() {
        StepOption option = new StepOption();
        option.setId(5);
        option.setOptionText("Option without next step");
        option.setNextStep(null);

        StepOptionDto dto = StepMapper.toDto(option);

        assertNotNull(dto);
        assertEquals(option.getId(), dto.getId());
        assertEquals(option.getOptionText(), dto.getOptionText());
        assertNull(dto.getNextStepId(), "nextStepId should be null when nextStep is null");
    }
}