package com.chwihae.domain.option;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface OptionRepository extends JpaRepository<OptionEntity, Long>, OptionRepositoryExtension {

    @Transactional
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "DELETE FROM option", nativeQuery = true)
    void physicallyDeleteAll();
}
