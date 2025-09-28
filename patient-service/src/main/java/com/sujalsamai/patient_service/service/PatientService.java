package com.sujalsamai.patient_service.service;

import com.sujalsamai.patient_service.dto.PatientRequestDTO;
import com.sujalsamai.patient_service.dto.PatientResponseDTO;
import com.sujalsamai.patient_service.exception.EmailAlreadyExistsException;
import com.sujalsamai.patient_service.exception.PatientNotFoundException;
import com.sujalsamai.patient_service.grpc.BillingServiceGrpcClient;
import com.sujalsamai.patient_service.mapper.PatientMapper;
import com.sujalsamai.patient_service.model.Patient;
import com.sujalsamai.patient_service.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient grpcClient;

    public PatientService(PatientRepository patientRepository, BillingServiceGrpcClient grpcClient) {
        this.patientRepository = patientRepository;
        this.grpcClient = grpcClient;
    }

    public List<PatientResponseDTO> getPatients() {
        List<Patient> patients = patientRepository.findAll();
        return patients.stream().map(PatientMapper::toDto).toList();
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequest) {
        if (patientRepository.existsByEmail(patientRequest.getEmail())) {
            throw new EmailAlreadyExistsException
                    ("A Patient with this email already exists: "
                            + patientRequest.getEmail());
        }

        Patient newPatient = patientRepository.save(PatientMapper.toModel(patientRequest));

        grpcClient.createBillingAccount(newPatient.getId().toString(),
                newPatient.getName(), newPatient.getEmail());

        return PatientMapper.toDto(newPatient);
    }

    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO) {
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
        return PatientMapper.toDto(updatePatient);
    }


    public void deletePatient(UUID id) {
        patientRepository.deleteById(id);
    }

}
