package com.example.bankcards.repository;

import com.example.bankcards.entity.BankCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BankCardRepository extends JpaRepository<BankCard, Long> {

    @EntityGraph(attributePaths = {"owner"})
    Page<BankCard> findByOwnerUsername(String username, Pageable pageable);

    @EntityGraph(attributePaths = {"owner"})
    Optional<BankCard> findByIdAndOwnerUsername(Long id, String username);

    @Override
    @EntityGraph(attributePaths = {"owner"})
    Page<BankCard> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"owner"})
    Page<BankCard> findByOwnerUsernameContainingIgnoreCase(String username, Pageable pageable);
}
