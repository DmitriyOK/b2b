package ru.protal.b2b.repository.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class OrderDao {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator ="orderseq")
    @SequenceGenerator(name = "orderseq", sequenceName = "orders_seq")
    @Column(name = "order_id", unique = true)
    Long orderId;

    @Column(name = "user_id", nullable = false, length = 35)
    Long userId;

    @Column(name = "action_id", nullable = false, length = 35)
    Long actionId;

    @Column(name = "entity_id", nullable = false, length = 35)
    Long entityId;

    @Column(name = "status_id", nullable = false, length = 35)
    Long statusId;

    @OrderBy("transition_id DESC")
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "transition_id", referencedColumnName = "order_id")
    List<OrderTransitionDao> transitions;

}
