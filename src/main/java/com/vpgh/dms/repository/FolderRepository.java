package com.vpgh.dms.repository;
import java.util.UUID;

import com.vpgh.dms.model.entity.Folder;
import com.vpgh.dms.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, UUID>, JpaSpecificationExecutor<Folder> {
    Optional<Folder> findById(UUID id);

    boolean existsByNameAndParentAndIsDeletedFalseAndIdNot(String name, Folder parent, UUID id);

    boolean existsByNameAndCreatedByAndParentIsNullAndIsDeletedFalseAndIdNot(String name, User createdBy, UUID id);

    Folder save(Folder folder);

    List<Folder> findByParentId(UUID id);

    Page<Folder> findAll(Specification<Folder> specification, Pageable pageable);

    List<Folder> findByIdIn(List<UUID> ids);

    List<Folder> findByParentAndIsDeletedFalse(Folder parent);

    List<Folder> findByParentAndIsDeletedTrue(Folder parent);

    Optional<Folder> findFirstByNameAndParentAndIsDeletedFalseAndCreatedBy(String name, Folder parent, User createdBy);
}
