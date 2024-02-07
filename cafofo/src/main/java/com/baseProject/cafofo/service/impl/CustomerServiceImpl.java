package com.baseProject.cafofo.service.impl;

import com.baseProject.cafofo.dto.FavouriteDto;
import com.baseProject.cafofo.dto.OfferRequestDto;
import com.baseProject.cafofo.entity.Customer;
import com.baseProject.cafofo.entity.Offer;
import com.baseProject.cafofo.entity.OfferStatus;
import com.baseProject.cafofo.entity.Property;
import com.baseProject.cafofo.exceptions.CafofoApplicationException;
import com.baseProject.cafofo.exceptions.CustomerException;
import com.baseProject.cafofo.exceptions.PropertyException;
import com.baseProject.cafofo.repo.CustomerRepo;
import com.baseProject.cafofo.repo.OfferRepo;
import com.baseProject.cafofo.repo.PropertyRepo;
import com.baseProject.cafofo.service.CustomerService;
import com.baseProject.cafofo.service.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepo customerRepository;

    private final PropertyRepo propertyRepository;

    private final ModelMapper modelMapper;

    private final EmailService emailService;

    private final OfferRepo offerRepo;


    @Override
    @Transactional
    public void save(Long customerId, OfferRequestDto offerRequest){
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(()->new CustomerException("Customer not found with id"+customerId));
        Property property = propertyRepository.findById(offerRequest.getPropertyId())
                .orElseThrow(()->new PropertyException("Property not found with id" + offerRequest.getPropertyId()));
        Offer offer = new Offer();
        offer.setCustomer(customer);
        offer.setOfferPrice(offerRequest.getOfferPrice());
        offer.setOfferDate(LocalDateTime.now());
        offer.setOfferStatus(OfferStatus.PENDING);
        offer.setProperty(property);
        offerRepo.save(offer);
    }

    @Override
    public String addToFavorites(Long userId, Long propertyId) {
        Customer customer = getCustomer(userId);
        Property property = getProperty(propertyId);

        if (customer != null && property != null) {
            // Check if the property is not already in the favorites list
            if (!customer.getFavoriteProperties().contains(property)) {
                customer.getFavoriteProperties().add(property);
                customerRepository.save(customer);
                return "Property added to favorites successfully.";
            } else {
                throw new CafofoApplicationException("Property is already in the favorites list.");
            }
        } else {
            throw new CafofoApplicationException("User or property not found.");
        }
    }

    @Override
    public String removeFromFavorites(Long userId, Long propertyId) {
        Customer customer = getCustomer(userId);
        Property property = getProperty(propertyId);
        if (customer != null && property != null) {
            // Check if the property is in the favorites list
            if (customer.getFavoriteProperties().contains(property)) {
                customer.getFavoriteProperties().remove(property);
                customerRepository.save(customer);
                return "Property remove to favorites successfully.";
            } else {
                throw new CafofoApplicationException("This Property is not in the favorites list.");
            }
        } else {
            throw new CafofoApplicationException("User or property not found.");
        }
    }

    @Override
    public List<FavouriteDto> getFavorites(Long userId) {
        Customer customer = getCustomer(userId);
        List<FavouriteDto> favouriteDtoList = new ArrayList<>();
        if (customer != null) {
            for (Property p : customer.getFavoriteProperties()) {
                FavouriteDto favouriteDto = modelMapper.map(p, FavouriteDto.class);
                favouriteDtoList.add(favouriteDto);
            }
            return favouriteDtoList;
        } else {
            throw new CafofoApplicationException("User not found.");
        }
    }

    private Customer getCustomer(Long userId) {
       // return customerRepository.findCustomerByUserId(userId);
        return customerRepository.findById(userId).get();
    }

    private Property getProperty(Long propertyId) {
        return propertyRepository.findById(propertyId).get();
    }

    @Override
    public List<Offer> getOffersByUser(Long userId) {
        return customerRepository.findOffersByCustomerId(userId);
    }

    @Override
    public String cancelOffer(Long offerId, Long userId) {
        offerValidation(offerId, userId);
        //email send
        emailToOwner(offerId, userId);
        customerRepository.cancelOffer(offerId, userId);
        return "Offer canceled successfully.";
    }

    @Override
    public String maintainOfferByPrice(Long offerId, double price, Long userId) {
        offerValidation(offerId, userId);
        customerRepository.maintainOfferByPrice(price,offerId, userId);
        return "Offer Price was successfully updated.";
    }

    private void offerValidation(Long offerId, Long userId) {
        Offer offer = customerRepository.checkOffer(userId, offerId);
        if (offer!= null) {
            // Check if the offer status is 'Pending'
            if (!OfferStatus.PENDING.equals(offer.getOfferStatus())) {
                throw new CafofoApplicationException("Unable to cancel the offer. Invalid offer status or user.");
            }
        } else {
            throw new CafofoApplicationException("Offer not found.");
        }

    }
    private void emailToOwner(Long offerId, Long userId){

        // Get customer and owner email addresses
        String ownerEmail=customerRepository.getOwnerEmail( offerId,  userId);
        Offer offer = customerRepository.checkOffer(userId, offerId);

        // Send email to owner
        String ownerSubject = "New Offer Received";
        String ownerBody = "Dear Owner, a new offer(Offer Price:"+offer.getOfferPrice()+"has been received for your property.(Property ID: "+offer.getProperty().getId() +"Name;"+offer.getProperty().getPropertyName()+").";

        emailService.sendEmail(ownerEmail, ownerSubject, ownerBody);
    }

}
