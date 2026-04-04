package me.rentsignal.location.repository;

import me.rentsignal.location.entity.Ri;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RiRepository extends JpaRepository<Ri, Long> {
}
