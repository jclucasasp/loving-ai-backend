package com.example.ai_dating_backend.responses;

public record Response (String messagePrompt,
                        String userId,
                        String name,
                        Integer age,
                        String ethnicity,
                        String gender,
                        String bio,
                        String personality)
{}
