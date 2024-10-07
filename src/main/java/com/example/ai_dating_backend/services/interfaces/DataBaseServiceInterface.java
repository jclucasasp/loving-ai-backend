package com.example.ai_dating_backend.services.interfaces;

import com.example.ai_dating_backend.profile.Profile;

import java.util.List;

public interface DataBaseServiceInterface {
    void purgeData();
    void updateAndSaveProfiles();
    void seedDataBase();
    void seedPersonalities();
}
