package com.chwihae.domain.commenter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommenterAliasRepository extends JpaRepository<CommenterAliasEntity, Long> {
}
