package ru.protal.b2b.service.messaging.kafkastub;

public @interface KafkaListener {
    String topics() default "";
}
