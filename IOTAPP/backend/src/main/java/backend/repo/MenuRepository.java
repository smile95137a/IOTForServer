package backend.repo;


import backend.entity.menu.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    boolean existsByMenuNameAndUrl(String menuName, String url);
}
