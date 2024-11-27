package ai.memory.ai.chat.memory.services.interfaces;

public interface DataBaseServiceInterface {
    void purgeData();
    void updateAndSaveProfiles();
    void seedDataBase();
    void seedPersonalities();
}
