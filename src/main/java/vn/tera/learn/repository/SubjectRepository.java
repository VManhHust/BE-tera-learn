package vn.tera.learn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tera.learn.entity.Subject;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

    List<Subject> findAllByActiveTrueOrderBySortOrderAsc();

    Optional<Subject> findByCodeAndActiveTrue(String code);
}
