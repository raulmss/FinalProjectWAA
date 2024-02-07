package com.baseProject.cafofo.repo;

import com.baseProject.cafofo.entity.Offer;
import com.baseProject.cafofo.entity.Owner;
import com.baseProject.cafofo.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Repository
public interface OwnerRepo extends JpaRepository<Owner,Long> {
    @Query("Select o.properties From Owner o join o.properties p join p.offers offer where offer.offerStatus = 'PENDING' and o.id = :userId")
    Collection<Property> getOwnerPropertiesByPlaced(Long userId);

    @Query("select po from Owner o join o.properties p join p.offers po where o.id=:ownerId and p.id=:propertiesId")
    List<Offer> findOffersByPropertiesId(Long ownerId, Long propertiesId);

}
