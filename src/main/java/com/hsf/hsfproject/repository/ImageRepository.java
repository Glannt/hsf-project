package com.hsf.hsfproject.repository;

import com.hsf.hsfproject.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ImageRepository extends JpaRepository<Image, UUID> {
//    List<Image> findByComputerItemIdAndColorId(UUID computerItemId, UUID color);
}

