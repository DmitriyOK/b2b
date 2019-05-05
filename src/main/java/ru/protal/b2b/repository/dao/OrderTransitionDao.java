package ru.protal.b2b.repository.dao;

import lombok.*;

import javax.persistence.*;


@Data
@ToString(exclude = "order")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_transition")
public class OrderTransitionDao implements Comparable<OrderTransitionDao>{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator ="transitionseq")
    @SequenceGenerator(name = "transitionseq", sequenceName = "transition_seq")
    @Column(name = "transition_id", unique = true)
    Long transitionId;

    @Column(name = "from_state_id", nullable = false, length = 35)
    Long fromStateId;

    @Column(name = "to_state_id", nullable = false, length = 35)
    Long toStateId;;

    @Column(name = "result", nullable = false, length = 35)
    Long resultId;

    @ManyToOne
    @JoinColumn(name="order_id", nullable=false)
    OrderDao order;

    @Override
    public int compareTo(OrderTransitionDao o) {
        return this.transitionId > o.transitionId ? -1 : this.transitionId < o.transitionId ? 1 : 0;
    }
}
