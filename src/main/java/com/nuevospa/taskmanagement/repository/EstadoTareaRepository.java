package com.nuevospa.taskmanagement.repository;

import com.nuevospa.taskmanagement.model.EstadoTarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstadoTareaRepository extends JpaRepository<EstadoTarea, Long> {
}
