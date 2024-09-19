package com.example.ai_dating_backend.responses;

public record Response (String messagePrompt,
                        String name,
                        Integer age,
                        String ethnicity,
                        String gender,
                        String bio)
{}
