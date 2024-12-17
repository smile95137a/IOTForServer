package backend.repo;


import backend.entity.channel.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {
	Optional<Channel> findByName(String name);

	Optional<Channel> findByuid(String uid);
}