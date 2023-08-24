package com.sid.gl.services.impl;

import com.sid.gl.model.ApplicationLog;
import com.sid.gl.repositories.ApplicationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplicationLogService {
    private final ApplicationLogRepository applicationLogRepository;

    public ApplicationLog saveLog(final ApplicationLog applicationLog){
        return applicationLogRepository.save(applicationLog);
    }

}
