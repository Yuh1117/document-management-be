package com.vpgh.dms.repository;

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
public interface FolderRepository extends JpaRepository<Folder, Integer>, JpaSpecificationExecutor<Folder> {
    Optional<Folder> findById(Integer id);

    boolean existsByNameAndParentAndIsDeletedFalseAndIdNot(String name, Folder parent, Integer id);

    boolean existsByNameAndCreatedByAndParentIsNullAndIsDeletedFalseAndIdNot(String name, User createdBy, Integer id);

    Folder save(Folder folder);

    List<Folder> findByParentId(Integer id);

    Page<Folder> findByParentAndCreatedByAndIsDeletedFalse(Folder parent, User createdBy, Pageable pageable);

    Page<Folder> findByParentAndCreatedByAndIsDeletedTrue(Folder parent, User createdBy, Pageable pageable);

    Page<Folder> findAll(Specification<Folder> specification, Pageable pageable);

    List<Folder> findByIdIn(List<Integer> ids);

    List<Folder> findByParentAndIsDeletedFalse(Folder parent);

    List<Folder> findByParentAndIsDeletedTrue(Folder parent);
}
