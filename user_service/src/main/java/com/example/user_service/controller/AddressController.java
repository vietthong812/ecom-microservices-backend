package com.example.user_service.controller;

import com.example.api.AddressApi;
import com.example.dto.AddressRequest;
import com.example.dto.AddressResponse;
import com.example.user_service.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AddressController implements AddressApi {

    @Autowired
    private AddressService addressService;


    @Override
    public ResponseEntity<AddressResponse> addAddress(String xUserId, AddressRequest addressRequest) {
        return new ResponseEntity<>(addressService.addAddress(xUserId, addressRequest), org.springframework.http.HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deleteAddress(String id, String xUserId) {
        return addressService.deleteAddress(id, xUserId);
    }

    @Override
    public ResponseEntity<AddressResponse> getAddressById(String id, String xUserId) {
        return ResponseEntity.ok(addressService.getAddressById(id, xUserId));
    }

    @Override
    public ResponseEntity<List<AddressResponse>> getUserAddresses(String xUserId) {
        return addressService.getUserAddresses(xUserId) != null ?
                ResponseEntity.ok(addressService.getUserAddresses(xUserId)) :
                ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<AddressResponse> updateAddress(String id, String xUserId, AddressRequest addressRequest) {
        return addressService.updateAddress(id, xUserId, addressRequest) != null ?
                ResponseEntity.ok(addressService.updateAddress(id, xUserId, addressRequest)) :
                ResponseEntity.notFound().build();
    }
}
