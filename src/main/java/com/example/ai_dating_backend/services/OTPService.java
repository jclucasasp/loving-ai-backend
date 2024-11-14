package com.example.ai_dating_backend.services;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@Log4j2
@Service
public class OTPService {

    private final Map<String, String> otpHashMap = new HashMap<>();

    public String getOtpHasMap(String email) {
        return otpHashMap.get(email);
    }

    public void setOtpHashMap(String email, String otp) {
        otpHashMap.put(email, otp);
    }

    public void otpTimer(String userEmail) {

        log.info("OTP Timer started for user: [{}]", userEmail);
        Timer timer = new Timer(userEmail);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                log.info("OTP expired for user: [{}]", userEmail);
                otpHashMap.remove(userEmail);
            }
        };
        // expires after 10 minutes
        timer.schedule(task, 600000);
    }
}
