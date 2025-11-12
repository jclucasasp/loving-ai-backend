package loving.ai.services;

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

    public void otpTimer(String userEmail, String otp) {

        log.info("OTP Timer started for user: [{}]", userEmail);

        if (otpHashMap.get(userEmail) != null) {
            log.info("OTP already exist, doing nothing...");
            return;
        }
        otpHashMap.putIfAbsent(userEmail, otp);

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
