package com.baseProject.cafofo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressOwnerPropertyCriterialRequest {
    private Long ownerid;
    private String country;
    private String state;
    private String city;
    private String street;
    private String number;
    private String zip;
}
