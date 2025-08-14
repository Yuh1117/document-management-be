package com.vpgh.dms.repository;

import com.vpgh.dms.model.entity.User;
import com.vpgh.dms.model.entity.UserGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserGroupMemberRepository extends JpaRepository<UserGroupMember, Integer> {

    List<UserGroupMember> findByUser(User user);
}

