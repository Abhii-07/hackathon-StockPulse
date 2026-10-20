package com.stockpulse.dto;

import com.stockpulse.entity.enums.SuggestionStatus;
import lombok.Data;

@Data
public class SuggestionStatusUpdateDTO {
    private SuggestionStatus status;
}