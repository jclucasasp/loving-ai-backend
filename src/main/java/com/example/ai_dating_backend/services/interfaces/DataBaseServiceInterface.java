package com.example.ai_dating_backend.services.interfaces;

public interface DataBaseServiceInterface {
    void purgeData();
    void updateAndSaveProfiles();
    void seedDataBase();
    void seedPersonalities();
}
