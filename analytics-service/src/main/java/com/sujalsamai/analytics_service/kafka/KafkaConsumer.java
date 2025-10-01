package com.sujalsamai.analytics_service.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

@Service
public class KafkaConsumer
{

   private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

   @KafkaListener(topics = "patient", groupId = "analytics-service")
   public void consumeEvent(@Payload byte[] event)
   {
      try
      {
         PatientEvent patientEvent = PatientEvent.parseFrom(event);
         //any business logic related to analytics ....

         log.info("Received Patient Event: [PatientId={},PatientName={},PatientEmail={}]",
                  patientEvent.getPatientId(),
                  patientEvent.getName(),
                  patientEvent.getEmailId());
      }
      catch (Exception e)
      {
         log.error("Error while deserializing event: {}", e.getMessage());
      }

   }
}
