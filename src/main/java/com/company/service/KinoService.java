package com.company.service;

import com.company.config.details.EntityDetails;
import com.company.dto.LikeCountDTO;
import com.company.dto.kino.KinoDTO;
import com.company.dto.kino.KinoSearchDTO;
import com.company.dto.kino.KinoUpdateDTO;
import com.company.dto.request.FindByNameDTO;
import com.company.entity.KinoEntity;
import com.company.enums.KinoStatus;
import com.company.exception.ItemNotFoundException;
import com.company.mapper.KinoMapper;
import com.company.repository.KinoRepository;
import com.company.repository.filter.KinoFilterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KinoService {

    private final KinoRepository kinoRepository;

    private final LikeService likeService;

    private final KinoFilterRepository kinoFilterRepository;


    /**
     * ADMIN
     */


    public KinoDTO  trailerUpload(KinoDTO dto){

        KinoEntity entity = new KinoEntity();
        entity.setName(dto.getName());
        entity.setCountry(dto.getCountry());
        entity.setStatus(KinoStatus.ACTIVE);
        entity.setTranslationLanguage(dto.getTranslationLanguage());
        entity.setType(dto.getType());
        entity.setCategoryId(dto.getCategoryId());
        entity.setPreviewAttachLink(dto.getPreviewAttachLink());
        entity.setVideoLink(dto.getVideoLink());
        entity.setVisible(true);

        kinoRepository.save(entity);

        return toDTO(entity);

    }

    public KinoDTO update(KinoUpdateDTO dto, String trailerId){

        KinoEntity entity = get(trailerId);

        if(entity == null){
            log.warn("Trailer not found! : {}",EntityDetails.getProfile());
            throw new ItemNotFoundException("Trailer not found!");
        }

        entity.setCountry(dto.getCountry());
        entity.setType(dto.getType());
        entity.setName(dto.getName());
        entity.setCategoryId(dto.getCategoryId());

        kinoRepository.save(entity);

        return toDTO(entity);
    }
    public Boolean delete(String trailerId) {

        KinoEntity entity = get(trailerId);
        if(entity == null){
            log.warn("Trailer not found! : {}",EntityDetails.getProfile());
            throw new ItemNotFoundException("Trailer not found!");
        }

        kinoRepository.updateDeleteDate(trailerId, LocalDateTime.now());

        return true;
    }

    public KinoEntity get(String trailerId) {
        return kinoRepository.findByIdAndDeletedDateIsNull(trailerId).orElse(null);
    }


    /**
     * PUBLIC
     */


    public KinoDTO getById(String trailerId) {

        KinoEntity entity = get(trailerId);

        if(entity == null){
            log.warn("Trailer not found! : {}",EntityDetails.getProfile());
            throw new ItemNotFoundException("Trailer not found!");
        }

        return toDTO(entity);
    }

    public PageImpl<KinoDTO> getAll(int page,int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));

        Page<KinoEntity> entityPage = kinoRepository.findAllByDeletedDateIsNull(pageable);

        List<KinoDTO> kinoDTOS = entityPage.stream().map(this::toDTO).toList();

        return new PageImpl<>(kinoDTOS,pageable,entityPage.getTotalElements());

    }

    public KinoDTO toDTO(KinoEntity entity){
        KinoDTO dto = new KinoDTO();

        dto.setId(entity.getId());
        dto.setCountry(entity.getCountry());
        dto.setCategoryId(entity.getCategoryId());
        dto.setName(entity.getName());
        dto.setPreviewAttachLink(entity.getPreviewAttachLink());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setTranslationLanguage(entity.getTranslationLanguage());
        dto.setVideoLink(entity.getVideoLink());
        dto.setType(entity.getType());

        return dto;
    }

    public KinoDTO toDTO(KinoMapper mapper){
        KinoDTO dto = new KinoDTO();

        dto.setId(mapper.getId());
        dto.setCountry(mapper.getCountry());
        dto.setCategoryId(mapper.getCategoryId());
        dto.setName(mapper.getName());
        dto.setPreviewAttachLink(mapper.getPreviewAttachLink());
        dto.setCreatedDate(mapper.getCreatedDate());
        dto.setTranslationLanguage(mapper.getTranslationLanguage());
        dto.setVideoLink(mapper.getVideoLink());
        dto.setType(mapper.getType());

        return dto;
    }

    public PageImpl<KinoDTO> getByCategoryId(int page, int size, String categoryID) {

        Pageable pageable = PageRequest.of(page, size);

        Page<KinoEntity> entityPage = kinoRepository.findByCategoryIdAndDeletedDateIsNull(categoryID, pageable);

        List<KinoDTO> kinoDTOS = entityPage.stream().map(this::toDTO).toList();

        return new PageImpl<>(kinoDTOS,pageable,entityPage.getTotalElements());
    }

    public PageImpl<KinoDTO> getByName(int page, int size, FindByNameDTO dto) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdDate"));
        Page<KinoEntity> entityPage = kinoRepository.findByNameAndDeletedDateIsNull(dto.getName(), pageable);
        List<KinoDTO> kinoDTOS = entityPage.stream().map(this::toDTO).toList();
        return new PageImpl<>(kinoDTOS,pageable,entityPage.getTotalElements());
    }


    public List<KinoDTO> filterKino(KinoSearchDTO dto){
        return  kinoFilterRepository.filter(dto).
                stream().map(this::toDTO).toList();
    }

    public PageImpl<KinoDTO> getByReyting(int page, int size) {
        return null;
    }

    public void updateViewCount(String kinoId) {
        KinoEntity entity = get(kinoId);
        if (entity.getViewCount() == null){
            entity.setViewCount(1L);
            kinoRepository.save(entity);
        } else {
            kinoRepository.increaseViewCount(kinoId);
        }

    }

    public PageImpl<KinoDTO> getByViewCount(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "viewCount"));
        Page<KinoEntity> entityPage = kinoRepository.findAllByDeletedDateIsNull(pageable);
        List<KinoDTO> kinoDTOS = entityPage.stream().map(this::toDTO).toList();
        return new PageImpl<>(kinoDTOS,pageable,entityPage.getTotalElements());
    }
}
