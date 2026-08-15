package com.example.user_service.mapper;

import com.example.dto.AddressRequest;
import com.example.dto.AddressResponse;
import com.example.user_service.entity.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public AddressResponse toAddressRespone(Address address){
        AddressResponse addressResponse =new AddressResponse();
        addressResponse.setCity(address.getCity());
        addressResponse.setId(address.getId());
        addressResponse.setDistrict(address.getDistrict());
        addressResponse.setStreet(address.getStreet());
        addressResponse.setIsDefault(address.getIsDefault());
        return addressResponse;
    }

}
