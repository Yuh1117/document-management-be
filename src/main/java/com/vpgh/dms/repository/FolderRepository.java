package com.vpgh.dms.repository;

import com.vpgh.dms.model.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Integer> {
    Optional<Folder> findById(Integer integer);

    boolean existsByNameAndParentAndIdNot(String name, Folder parent, Integer excludeId);

    Folder save(Folder folder);
}
