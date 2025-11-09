package com.javarush.khmelov.mapper;

import com.javarush.khmelov.dto.StepDto;
import com.javarush.khmelov.dto.StepOptionDto;
import com.javarush.khmelov.entity.Step;
import com.javarush.khmelov.entity.StepOption;

import java.util.List;

public class StepMapper {

    public static StepDto toDto(Step step) {
        if (step == null) return null;

        StepDto dto = new StepDto();
        dto.setId(step.getId());
        dto.setText(step.getText());
        dto.setFinish(step.getFinish());
        dto.setWin(step.getWin());
        dto.setOptions(toDtoOptions(step.getOptions())); // переиспользуем метод
        return dto;
    }

    private static List<StepOptionDto> toDtoOptions(List<StepOption> options) {
        if (options == null) return List.of();
        return options.stream()
                .map(StepMapper::toDto)
                .toList();
    }

    public static StepOptionDto toDto(StepOption option) {
        if (option == null) return null;

        StepOptionDto dto = new StepOptionDto();
        dto.setId(option.getId());
        dto.setOptionText(option.getOptionText());
        dto.setNextStepId(option.getNextStep() != null ? option.getNextStep().getId() : null);
        return dto;
    }
}

