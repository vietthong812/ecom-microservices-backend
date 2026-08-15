package com.example.user_service.service;

import com.example.api.AddressApi;
import com.example.dto.AddressRequest;
import com.example.dto.AddressResponse;
import com.example.user_service.entity.Address;
import com.example.user_service.mapper.AddressMapper;
import com.example.user_service.repository.AddressRepository;
import com.example.user_service.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private AddressMapper addressMapper;

    public AddressResponse addAddress(String xUserId, AddressRequest addressRequest) {
        Address address = new Address();
        address.setUserProfile(userProfileRepository.findById(xUserId).orElseThrow(() -> new RuntimeException("User not found")));
        address.setStreet(addressRequest.getStreet());
        address.setCity(addressRequest.getCity());
        address.setDistrict(addressRequest.getDistrict());
        address.setIsDefault(addressRequest.getIsDefault());
        Address savedAddress = addressRepository.save(address);
        return addressMapper.toAddressRespone(savedAddress);
    }

    public ResponseEntity<Void> deleteAddress(String id, String xUserId) {
        Address address = addressRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));
        if (!address.getUserProfile().getId().equals(xUserId)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        addressRepository.delete(address);
        return ResponseEntity.ok().build();
    }

    public List<AddressResponse> getUserAddresses(String xUserId) {
        List<Address> addresses = addressRepository.findByUserProfileId(xUserId);
        return addresses.stream()
                .map(addressMapper::toAddressRespone)
                .toList();
    }

    public AddressResponse updateAddress(String id, String xUserId, AddressRequest addressRequest) {
        Address address = addressRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));
        if (!address.getUserProfile().getId().equals(xUserId)) {
            throw new RuntimeException("Forbidden");
        }
        address.setStreet(addressRequest.getStreet());
        address.setCity(addressRequest.getCity());
        address.setDistrict(addressRequest.getDistrict());
        address.setIsDefault(addressRequest.getIsDefault());
        Address updatedAddress = addressRepository.save(address);
        return addressMapper.toAddressRespone(updatedAddress);
    }

    public AddressResponse getAddressById(String id, String xUserId) {
        Address address = addressRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));
        if (!address.getUserProfile().getId().equals(xUserId)) {
            throw new RuntimeException("Forbidden");
        }
        return addressMapper.toAddressRespone(address);
    }
}
