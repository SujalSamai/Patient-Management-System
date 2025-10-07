package com.sujalsamai.patient_service.service;

import com.sujalsamai.patient_service.dto.PatientRequestDTO;
import com.sujalsamai.patient_service.dto.PatientResponseDTO;
import com.sujalsamai.patient_service.exception.EmailAlreadyExistsException;
import com.sujalsamai.patient_service.exception.PatientNotFoundException;
import com.sujalsamai.patient_service.grpc.BillingServiceGrpcClient;
import com.sujalsamai.patient_service.kafka.KafkaProducer;
import com.sujalsamai.patient_service.mapper.PatientMapper;
import com.sujalsamai.patient_service.model.Patient;
import com.sujalsamai.patient_service.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService
{

    private static final Logger log = LoggerFactory.getLogger(PatientService.class);
    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient grpcClient;
    private final KafkaProducer kafkaProducer;

    public PatientService(PatientRepository patientRepository,
        BillingServiceGrpcClient grpcClient,
        KafkaProducer kafkaProducer)
    {
        this.patientRepository = patientRepository;
        this.grpcClient = grpcClient;
        this.kafkaProducer = kafkaProducer;
    }

    public List<PatientResponseDTO> getPatients()
    {
        List<Patient> patients = patientRepository.findAll();
        return patients.stream().map(PatientMapper::toDto).toList();
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequest)
    {
        if (patientRepository.existsByEmail(patientRequest.getEmail())) {
            throw new EmailAlreadyExistsException
                ("A Patient with this email already exists: "
                    + patientRequest.getEmail());
        }

        Patient newPatient = patientRepository.save(PatientMapper.toModel(patientRequest));
        log.info("Created patient as: {}, sending patientData to notify other services", newPatient);

        grpcClient.createBillingAccount(newPatient.getId().toString(),
            newPatient.getName(), newPatient.getEmail());

        kafkaProducer.sendEvent(newPatient);

        return PatientMapper.toDto(newPatient);
    }

    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO)
    {
        Patient patient = patientRepository.findById(id).orElseThrow(
            () -> new PatientNotFoundException("Patient not found with ID: " + id));

        if (patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail(), id)) {
            throw new EmailAlreadyExistsException
                ("A Patient with this email already exists: "
                    + patientRequestDTO.getEmail());
        }

        patient.setName(patientRequestDTO.getName());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));

        Patient updatePatient = patientRepository.save(patient);
        log.info("Updated patient: {}", updatePatient);
        return PatientMapper.toDto(updatePatient);
    }


    public void deletePatient(UUID id)
    {
        patientRepository.deleteById(id);
        log.info("Patient with id: {}, deleted successfully", id);
    }

}
