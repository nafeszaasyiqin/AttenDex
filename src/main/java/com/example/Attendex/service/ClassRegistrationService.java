package com.example.Attendex.service;

import com.example.Attendex.model.ClassEntity;
import com.example.Attendex.model.ClassRegistration;
import com.example.Attendex.repo.ClassUserRepo;
import com.example.Attendex.repo.ClassRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClassRegistrationService {

    @Autowired
    private ClassUserRepo classRegistrationRepository;

    public List<String> getRegisteredStudentsUsernames(ClassEntity classEntity) {
        List<ClassRegistration> registrations = classRegistrationRepository.findByClassEntity(classEntity);
        return registrations.stream()
                .map(registration -> registration.getStudent().getUsername())
                .collect(Collectors.toList());
    }
}
