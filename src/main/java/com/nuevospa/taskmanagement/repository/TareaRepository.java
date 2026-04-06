package com.nuevospa.taskmanagement.repository;

import com.nuevospa.taskmanagement.model.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long>, JpaSpecificationExecutor<Tarea> {

    List<Tarea> findByEstadoId(Long estadoId);

    List<Tarea> findByUsuarioAsignadoId(Long usuarioAsignadoId);

    List<Tarea> findByEstadoIdAndUsuarioAsignadoId(Long estadoId, Long usuarioAsignadoId);
}
