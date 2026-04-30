package com.fanproduction.gui.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateComponentClassRequest {
    private String name;
    private String description;
}
