package com.javarush.khmelov.mapper;


import com.javarush.khmelov.dto.StepOptionDto;
import com.javarush.khmelov.entity.StepOption;

public class StepOptionMapper {

    public static StepOptionDto toDto(StepOption option) {
        if (option == null) return null;

        StepOptionDto dto = new StepOptionDto();
        dto.setId(option.getId());
        dto.setOptionText(option.getOptionText());
        dto.setNextStepId(option.getNextStep() != null ? option.getNextStep().getId() : null);
        return dto;
    }
}
