package com.veterinaria.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@NoArgsConstructor
public class ReniecResponseDTO {

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("first_last_name")
    private String firstLastName;

    @JsonProperty("second_last_name")
    private String secondLastName;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("document_number")
    private String documentNumber;

    @JsonProperty("existe_en_bd")
    private Boolean existeEnBd = false;

    @JsonProperty("email")
    private String email;

    @JsonProperty("telefono")
    private String telefono;
}
