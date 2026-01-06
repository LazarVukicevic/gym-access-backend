package com.lazar.gymaccess.repository;

import com.lazar.gymaccess.model.member.Member;
import com.lazar.gymaccess.model.member.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberRepository extends JpaRepository<Member, UUID> {
    Optional<Member> findByEmailIgnoreCase(String email);
    List<Member> findByStatus(MemberStatus status);

    @Query("""
        SELECT m from Member m
        WHERE
            (:query is NULL OR
             LOWER(m.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR
             LOWER(m.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR
             LOWER(m.email) LIKE LOWER(CONCAT('%', :query, '%')))
        AND
            (:status IS NULL OR m.status = :status)
    """)
    List<Member> searchMembers(
            @Param("query") String query,
            @Param("status") MemberStatus status
    );

    Optional<Member> findByQrTokenHash(String qrTokenHash);

    boolean existsByQrTokenHash(String qrTokenHash);
}
