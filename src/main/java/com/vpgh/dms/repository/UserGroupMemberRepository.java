package com.vpgh.dms.repository;
import java.util.UUID;

import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.model.entity.UserGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserGroupMemberRepository extends JpaRepository<UserGroupMember, UUID> {

    List<UserGroupMember> findByUser(User user);
}
