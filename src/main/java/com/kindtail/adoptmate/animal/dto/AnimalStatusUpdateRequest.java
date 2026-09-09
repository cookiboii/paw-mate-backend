package com.kindtail.adoptmate.animal.dto;


import com.kindtail.adoptmate.animal.domain.Status;
import jakarta.validation.constraints.NotNull;

public record AnimalStatusUpdateRequest(@NotNull Status status){
}
