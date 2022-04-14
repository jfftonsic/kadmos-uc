package com.example.db.relational.repository;

import com.example.db.relational.entity.BalanceUpdateUndoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BalanceUpdateUndoRepository extends JpaRepository<BalanceUpdateUndoEntity, UUID> {
}
