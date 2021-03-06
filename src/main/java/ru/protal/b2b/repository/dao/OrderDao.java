package ru.protal.b2b.repository.dao;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@ToString(exclude = "user")
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

    @Column(name = "action_id", nullable = false, length = 35)
    Long actionId;

    @Column(name = "entity_id", nullable = false, length = 35)
    Long entityId;

    @Column(name = "status_id", nullable = false, length = 35)
    Long statusId;

    @OrderBy("transition_id DESC")
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "order")
    List<OrderTransitionDao> transitions;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "user_id")
    UserDao user;

}
