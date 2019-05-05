package ru.protal.b2b.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.protal.b2b.repository.dao.OrderDao;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderDao,Long> {

    List<OrderDao> findAllByStatusId(Long statusId);
}
