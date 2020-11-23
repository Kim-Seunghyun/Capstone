package practice.capston.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import practice.capston.domain.entity.Resources;

public interface ResourceRepository extends JpaRepository<Resources, Long> {
}
