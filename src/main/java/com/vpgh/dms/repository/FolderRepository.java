package com.vpgh.dms.repository;

import com.vpgh.dms.model.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Integer> {
    Optional<Folder> findById(Integer integer);

    boolean existsByNameAndParentAndIdNot(String name, Folder parent, Integer excludeId);

    Folder save(Folder folder);

    List<Folder> findByParentId(Integer id);

    List<Folder> findByParentIdAndIsDeletedFalse(Integer parentId);   // Chỉ lấy thư mục chưa bị xoá mềm

    List<Folder> findByParentIdAndIsDeletedTrue(Integer parentId);
}
